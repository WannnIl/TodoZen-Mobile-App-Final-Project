package com.projeku.finalproject.model;

import com.google.gson.annotations.SerializedName;

public class Quote {

    @SerializedName("q")
    private String quoteText;

    @SerializedName("a")
    private String author;

    public String getQuoteText() {
        return quoteText;
    }

    public String getAuthor() {
        return author;
    }
}