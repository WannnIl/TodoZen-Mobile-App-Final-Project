package com.projeku.finalproject.network;

import com.projeku.finalproject.model.Quote;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("api/random")
    Call<List<Quote>> getRandomQuote();

    @GET("api/today")
    Call<List<Quote>> getQuoteOfTheDay();
}
