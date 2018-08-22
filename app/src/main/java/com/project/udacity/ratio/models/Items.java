package com.project.udacity.ratio.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mehseti on 27.7.2018.
 */

public class Items {
    @SerializedName("volumeInfo")
    private Book book;

    @SerializedName("id")
    String id;

    public Book getBook() {
        book.setId(id);
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
