package com.project.udacity.ratio.data;

import com.google.gson.JsonArray;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by mehseti on 26.7.2018.
 */

public interface CollectionApi {
    @GET("movie/popular/?")
    public io.reactivex.Observable<MovieResponse> listMovies(@Query("api_key") String keyApi);

    @GET("movie?")
    public io.reactivex.Observable<MovieResponse> getMovieByTitle(@Query("api_key") String keyApi,@Query("query") String query);

    // https://www.googleapis.com/books/v1/volumes?q=printType=books&orderBy=newest

    @GET("v1/volumes?q=\"\"&printType=books&langRestrict=en")
    public io.reactivex.Observable<BookResponse> listBooks(@Query("key") String keyApi);


    @GET("v1/volumes?printType=books&langRestrict=en")
    public io.reactivex.Observable<BookResponse> getBookByTitle(@Query("q") String searchText,@Query("key") String keyApi);


    @GET("shows?page=1")
    public io.reactivex.Observable<JsonArray> listTvSeries();

    @GET("shows?")
    public io.reactivex.Observable<JsonArray> getTvSeriesByTitle(@Query("q") String searchText);


   /* @GET("movie/?")
    public io.reactivex.Observable<MovieResponse> getMovieByTitle( @Query("api_key") String keyApi,@Query("query") String query);*/
}
