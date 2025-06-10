package com.projeku.finalproject.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.projeku.finalproject.model.Quote;
import com.projeku.finalproject.network.ApiService;
import com.projeku.finalproject.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuoteRepository {

    private ApiService apiService;
    private Application application;
    private SharedPreferences sharedPreferences;
    private ExecutorService executor;
    private Handler handler;

    private static final String PREFS_NAME = "QuotePrefs";
    private static final String KEY_QUOTE_TEXT = "quote_text";
    private static final String KEY_QUOTE_AUTHOR = "quote_author";
    private static final String KEY_LAST_FETCH_DATE = "last_fetch_date";

    public QuoteRepository(Application application) {
        this.application = application;
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
        this.sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
    }

    public LiveData<Quote> getQuoteOfTheDay() {
        MutableLiveData<Quote> quoteData = new MutableLiveData<>();
        executor.execute(() -> {
            if (isNetworkAvailable()) {
                String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String lastFetchDate = sharedPreferences.getString(KEY_LAST_FETCH_DATE, "");

                if (!todayDate.equals(lastFetchDate)) {
                    // Jika tanggal berbeda, ambil kutipan baru dari API
                    fetchQuoteOfTheDayFromApi(quoteData);
                } else {
                    // Jika tanggal sama, ambil dari SharedPreferences
                    loadQuoteFromPrefs(quoteData);
                }
            } else {
                // Jika tidak ada jaringan, ambil dari SharedPreferences
                loadQuoteFromPrefs(quoteData);
                handler.post(() -> Toast.makeText(application, "No network. Showing cached quote.", Toast.LENGTH_SHORT).show());
            }
        });
        return quoteData;
    }

    public LiveData<Quote> getRandomQuote() {
        MutableLiveData<Quote> quoteData = new MutableLiveData<>();
        executor.execute(() -> {
            if (isNetworkAvailable()) {
                apiService.getRandomQuote().enqueue(new Callback<List<Quote>>() {
                    @Override
                    public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            quoteData.postValue(response.body().get(0));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Quote>> call, Throwable t) {
                        handler.post(() -> Toast.makeText(application, "Failed to fetch a new quote.", Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                handler.post(() -> Toast.makeText(application, "No network connection to get a new quote.", Toast.LENGTH_SHORT).show());
            }
        });
        return quoteData;
    }


    private void fetchQuoteOfTheDayFromApi(MutableLiveData<Quote> quoteData) {
        apiService.getQuoteOfTheDay().enqueue(new Callback<List<Quote>>() {
            @Override
            public void onResponse(Call<List<Quote>> call, Response<List<Quote>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Quote quote = response.body().get(0);
                    quoteData.postValue(quote);
                    saveQuoteToPrefs(quote);
                } else {
                    loadQuoteFromPrefs(quoteData); // Gagal, coba muat dari cache
                }
            }

            @Override
            public void onFailure(Call<List<Quote>> call, Throwable t) {
                loadQuoteFromPrefs(quoteData); // Gagal, coba muat dari cache
            }
        });
    }

    private void saveQuoteToPrefs(Quote quote) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_QUOTE_TEXT, quote.getQuoteText());
        editor.putString(KEY_QUOTE_AUTHOR, quote.getAuthor());
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editor.putString(KEY_LAST_FETCH_DATE, todayDate);
        editor.apply();
    }

    private void loadQuoteFromPrefs(MutableLiveData<Quote> quoteData) {
        String quoteText = sharedPreferences.getString(KEY_QUOTE_TEXT, "The journey of a thousand miles begins with a single step.");
        String author = sharedPreferences.getString(KEY_QUOTE_AUTHOR, "Lao Tzu");
        Quote cachedQuote = new Quote() {
            @Override
            public String getQuoteText() {
                return quoteText;
            }
            @Override
            public String getAuthor() {
                return author;
            }
        };
        quoteData.postValue(cachedQuote);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
