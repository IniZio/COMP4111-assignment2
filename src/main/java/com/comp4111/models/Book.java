package com.comp4111.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

public class Book {
  private String id;
  public String title;
  public String author;
  public String publisher;
  public Integer year;
  public Boolean available;

  public Book () {
    if (!(this.id != null && !this.id.isEmpty())) {
      this.id = UUID.randomUUID().toString();
    }
  }

  public String getId () {
    return this.id;
  }

  // public Book Id (String id) {
  //   this.id = id;
  //   return this;
  // }

  public Book Title (String title) {
    this.title = title;
    return this;
  }

  public Book Author (String author) {
    this.author = author;
    return this;
  }

  public Book Publisher (String publisher) {
    this.publisher = publisher;
    return this;
  }

  public Book Year (Integer year) {
    this.year = year;
    return this;
  }

  public Book Available (Boolean available) {
    this.available = available;
    return this;
  }

  public Map<String, Object> serialize() {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.convertValue(this, Map.class);
  }
}
