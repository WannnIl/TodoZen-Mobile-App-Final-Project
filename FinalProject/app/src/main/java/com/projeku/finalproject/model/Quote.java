package com.projeku.finalproject.model;

import com.google.gson.annotations.SerializedName;

// Kelas untuk menampung hasil dari ZenQuotes API
public class Quote {

    @SerializedName("q")
    private String quoteText;

    @SerializedName("a")
    private String author;

    // Getter
    public String getQuoteText() {
        return quoteText;
    }

    public String getAuthor() {
        return author;
    }
}