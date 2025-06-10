package com.projeku.finalproject.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.projeku.finalproject.model.Quote;
import com.projeku.finalproject.repository.QuoteRepository;


public class QuoteViewModel extends AndroidViewModel {

    private QuoteRepository repository;
    private LiveData<Quote> quoteOfTheDay;

    public QuoteViewModel(@NonNull Application application) {
        super(application);
        repository = new QuoteRepository(application);
        this.quoteOfTheDay = repository.getQuoteOfTheDay();
    }

    public LiveData<Quote> getQuoteOfTheDay() {
        return this.quoteOfTheDay;
    }

    public LiveData<Quote> getRandomQuote() {
        // Setiap kali dipanggil, ia akan mengambil kutipan acak yang baru
        return repository.getRandomQuote();
    }
}
