package com.project.udacity.ratio.util;

import android.app.IntentService;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.project.udacity.ratio.data.BookResponse;
import com.project.udacity.ratio.data.CollectionApi;
import com.project.udacity.ratio.data.MovieResponse;
import com.project.udacity.ratio.models.Book;
import com.project.udacity.ratio.models.Items;
import com.project.udacity.ratio.models.Movie;
import com.project.udacity.ratio.models.Serial;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchIntentService extends IntentService {
    String searchText;
    List<Movie> movies;
    List<Serial> serials;
    List<Book> books;

    public SearchIntentService() {
        super("Search Intent Service");
    }

    public SearchIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        searchText = intent.getStringExtra("searchText");
        getBookFromApiByTitle();
    }

    private void getBookFromApiByTitle() {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ApiVariables.bookUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.client(httpClient).build();

        CollectionApi booksApi = retrofit.create(CollectionApi.class);
        io.reactivex.Observable<BookResponse> call = booksApi
                .getBookByTitle(searchText, ApiVariables.bookApiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        call.map(new Function<BookResponse, List<Items>>() {
            @Override
            public List<Items> apply(BookResponse bookResponse) throws Exception {
                return bookResponse.getItems();
            }
        })
                .subscribe
                        (
                                new Consumer<List<Items>>() {
                                    @Override
                                    public void accept(List<Items> items) throws Exception {

                                        books = new ArrayList<>();

                                        for (Items i : items) {
                                            books.add(i.getBook());
                                        }

                                        getMovieFromApiByTitle();
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Log.i("error", "RxJava2, HTTP Error: book " + throwable.getLocalizedMessage());
                                    }
                                }
                        );
    }

    private void getMovieFromApiByTitle() {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ApiVariables.movieByTitle)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.client(httpClient).build();
        CollectionApi moviesApi = retrofit.create(CollectionApi.class);
        io.reactivex.Observable<MovieResponse> call = moviesApi.getMovieByTitle(ApiVariables.movieApiKey, searchText).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        call.map(new Function<MovieResponse, List<Movie>>() {
            @Override
            public List<Movie> apply(MovieResponse movieResponse) throws Exception {
                return movieResponse.getMovies();
            }
        })
                .subscribe
                        (
                                new Consumer<List<Movie>>() {
                                    @Override
                                    public void accept(List<Movie> movieList) throws Exception {
                                        movies = movieList;
                                        getTvSeriesFromApiByTitle();
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Log.i("error", "RxJava2, HTTP Error: movie " + throwable.getMessage());
                                    }
                                }
                        );
    }

    private void getTvSeriesFromApiByTitle() {
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(ApiVariables.serialByTitle)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.client(httpClient).build();
        CollectionApi tvSeriesApi = retrofit.create(CollectionApi.class);
        io.reactivex.Observable<JsonArray> call = tvSeriesApi.getTvSeriesByTitle(searchText).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        call.map(new Function<JsonArray, List<Serial>>() {
            @Override
            public List<Serial> apply(JsonArray jsonArray) throws Exception {
                Gson gson = new Gson();
                List<Serial> serials = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    Serial serial = gson.fromJson(jsonArray.get(i).getAsJsonObject().get("show"), Serial.class);
                    serials.add(serial);
                }

                return serials;
            }
        })
                .subscribe
                        (
                                new Consumer<List<Serial>>() {
                                    @Override
                                    public void accept(List<Serial> serialList) throws Exception {
                                       serials = new ArrayList<>();

                                        for (Serial serial : serialList) {
                                            if (serial.getPosterPath() != null) {
                                                serials.add(serial);
                                            }
                                        }

                                        sendCollections();



                                        //displayTvSeries(serialsTemp, null);
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Log.i("error", "RxJava2, HTTP Error: serial" + throwable.getMessage());
                                    }
                                }
                        );
    }

    private void sendCollections() {
        Intent intent = new Intent();
        intent.setAction("SEARCH");
        intent.putParcelableArrayListExtra("movies", (ArrayList<? extends Parcelable>) movies);
        intent.putParcelableArrayListExtra("books", (ArrayList<? extends Parcelable>) books);
        intent.putParcelableArrayListExtra("serials", (ArrayList<? extends Parcelable>) serials);
        sendBroadcast(intent);
    }

}
