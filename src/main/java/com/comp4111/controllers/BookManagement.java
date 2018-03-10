package com.comp4111.controllers;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.comp4111.models.Book;

@Path("books")
public class BookManagement {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Book getBook () {
        Book book = new Book();
        book.setTitle("XYZ");
        return book;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBook (Book book) {
        URI uri;
        ResponseBuilder response = Response
            .status(Response.Status.CREATED)
            .entity(book);

        try {
             uri = new URI("books/" + 1);
             response
                .location(uri);
        } catch (Exception e) {
        }

        return response.build();
    }
}
