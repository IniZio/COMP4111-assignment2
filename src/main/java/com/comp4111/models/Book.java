package com.comp4111.models;

public class Book {
  String id;
  String title;
  String author;
  String publisher;
  Integer year;
  Boolean available;

  public Book () {}

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }
}
