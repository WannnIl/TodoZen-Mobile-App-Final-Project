package com.projeku.finalproject.network;

import com.projeku.finalproject.model.Quote;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    // Endpoint untuk mendapatkan kutipan acak
    @GET("api/random")
    Call<List<Quote>> getRandomQuote();

    // Endpoint untuk mendapatkan kutipan hari ini
    @GET("api/today")
    Call<List<Quote>> getQuoteOfTheDay();
}
