package com.project.udacity.ratio.util;

import com.project.udacity.ratio.BuildConfig;

/**
 * Created by mehseti on 26.7.2018.
 */

public class ApiVariables {
    public static String movieUrl = "http://api.themoviedb.org/3/";
    public static String movieApiKey = BuildConfig.MOVIE_API_KEY;
    public static String bookUrl = "https://www.googleapis.com/books/";
    public static String bookApiKey = BuildConfig.BOOK_API_KEY;
    public static String serialUrl = "http://api.tvmaze.com/";
    public static String movieByTitle = "https://api.themoviedb.org/3/search/";
    public static String bookByTitle = "https://www.googleapis.com/books/v1/volumes?q=";
    public static String serialByTitle = "http://api.tvmaze.com/search/";
    public static String imagePathForMovie = "http://image.tmdb.org/t/p/w342//";

}
