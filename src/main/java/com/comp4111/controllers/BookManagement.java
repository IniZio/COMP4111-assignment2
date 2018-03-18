package com.comp4111.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.comp4111.models.Book;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

@Path("books")
public class BookManagement {
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference booksRef = database.getReference("books");

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void getBook (
        @DefaultValue("") @QueryParam("id") String id,
        @DefaultValue("") @QueryParam("title") String title,
        @DefaultValue("") @QueryParam("author") String author,
        @DefaultValue("-1") @QueryParam("limit") Integer limit,
        @DefaultValue("") @QueryParam("sortby") String sortby,
        @DefaultValue("") @QueryParam("order") String order,        
        @Suspended final AsyncResponse response
    ) {
        Query booksQuery = booksRef;
    
        if (limit != -1) {
            booksQuery = booksQuery.limitToLast(limit);
        }

        final Boolean sortable = sortby != null && !sortby.isEmpty() && order != null && !order.isEmpty();
        if (sortable) {
            booksQuery = booksQuery.orderByChild(sortby);
        }

        booksQuery.addListenerForSingleValueEvent(new ValueEventListener(){
            
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        ArrayList<Book> books = new ArrayList<Book>();
                        for (DataSnapshot bookSnapshot: snapshot.getChildren()) {
                            Book entry = bookSnapshot.getValue(Book.class);
                            Boolean flag = true;
                            if (author != null && !author.isEmpty() && !entry.author.equals(author)) {
                                flag = false;
                            }
                            if (id != null && !id.isEmpty() && !entry.getId().equals(id)) {
                                flag = false;
                            }
                            if (title != null && !title.isEmpty() && !entry.title.equals(title)) {
                                flag = false;
                            }
                            if (flag) {
                                books.add(entry);
                            }
                        }

                        
                        if (books.size() <= 0) {
                            response.resume(
                                Response
                                .status(Response.Status.NO_CONTENT)
                                .build()
                            );
                            return;
                        }

                        if (sortable && order.equals("desc")) {
                            Collections.reverse(books);
                        }

                        HashMap<String, Object> result = new HashMap<String, Object>();
                        result.put("FoundBooks", books.size());
                        result.put("Results", books.toArray());

                        response.resume(
                            Response
                            .status(Response.Status.CREATED)
                            .entity(result)
                            .build()
                        );
                    } catch (Exception e) {
                        System.out.println("cant send response with error: " + e);
                    }
                }
            
                @Override
                public void onCancelled(DatabaseError error) {
                    
                }
            });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void createBook (final Book book, @Suspended final AsyncResponse response) {
        // Map<String, Object> books = new HashMap<>();
        // books.put(book.id, book.serialize());

        booksRef.orderByChild("title").equalTo(book.title).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChildren()) {
                    try {
                        DatabaseReference bookRef = booksRef.child(book.getId());
                        bookRef.setValue(book.serialize(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                System.out.println("Data could not be saved " + databaseError.getMessage());
                                } else {
                                    System.out.println("Data saved successfully.");
                                    try {
                                        response.resume(
                                            Response
                                            .status(Response.Status.CREATED)
                                            .entity(book)
                                            .location(new URI("books/" + book.getId()))
                                            .build()
                                        );
                                    } catch (Exception e) {
                                        System.out.println("cant send response with error: " + e);
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        System.out.println("cant set with error: " + e);
                    }
                    
                } else {
                    try {
                        response.resume(
                            Response
                            .status(Response.Status.CONFLICT)
                            .header("duplicate-record", "books/" + snapshot.getChildren().iterator().next().getValue(Book.class).getId())
                            .location(new URI("books/" + snapshot.getChildren().iterator().next().getValue(Book.class).getId()))
                            .build()
                        );
                    } catch (Exception e) {
                        System.out.println("cant send response with error: " + e);
                    }
                }
            }
        
            @Override
            public void onCancelled(DatabaseError error) {
                
            }
        });
    }

    @PUT
    @Path("{bookId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void modifyBook (final Map<String, Object> modification, @PathParam("bookId") String bookId, @Suspended final AsyncResponse response) {
        booksRef.orderByChild("id").equalTo(bookId).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    try {
                        Book book = snapshot.getChildren().iterator().next().getValue(Book.class).Id(bookId);
                        // Want to borrow but not available
                        if (
                            (!book.available && !(Boolean) modification.get("Available"))
                            || (book.available && (Boolean) modification.get("Available"))
                        ) {
                            try {
                                response.resume(
                                    Response
                                    .status(Response.Status.BAD_REQUEST)
                                    .build()
                                );
                                return;
                            } catch (Exception e) {
                                System.out.println("cant send response with error: " + e);
                            }
                        }
                        DatabaseReference bookRef = booksRef.child(book.getId());
                        book.Available((Boolean) modification.get("Available"));
                        bookRef.setValue(book.serialize(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                System.out.println("Data could not be saved " + databaseError.getMessage());
                                } else {
                                    System.out.println("Data saved successfully.");
                                    try {
                                        response.resume(
                                            Response
                                            .status(Response.Status.OK)
                                            .build()
                                        );
                                    } catch (Exception e) {
                                        System.out.println("cant send response with error: " + e);
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {}
                } else {
                    try {
                        response.resume(
                            Response
                            .status(404, "No book record")
                            .build()
                        );
                    } catch (Exception e) {
                        System.out.println("cant send response with error: " + e);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                
            }
        });       
    }

    @DELETE
    @Path("{bookId}")
    public void deleteBook (@PathParam("bookId") String bookId, @Suspended final AsyncResponse response) {
        booksRef.orderByChild("id").equalTo(bookId).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChildren()) {
                    booksRef.child(bookId).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                System.out.println("Data could not be deleted " + databaseError.getMessage());
                                } else {
                                    System.out.println("Data deleted successfully.");
                                    try {
                                        response.resume(
                                            Response
                                            .status(Response.Status.OK)
                                            .build()
                                        );
                                    } catch (Exception e) {
                                        System.out.println("cant send response with error: " + e);
                                    }
                                }
                            }
                    });
                } else {
                    try {
                        response.resume(
                            Response
                            .status(404, "No book record")
                            .build()
                        );
                    } catch (Exception e) {
                        System.out.println("cant send response with error: " + e);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                
            }
        });
    }
}
