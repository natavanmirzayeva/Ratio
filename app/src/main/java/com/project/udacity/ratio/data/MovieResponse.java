package com.project.udacity.ratio.data;

import com.google.gson.annotations.SerializedName;
import com.project.udacity.ratio.models.Movie;

import java.util.List;

/**
 * Created by mehseti on 26.7.2018.
 */

public class MovieResponse {
    @SerializedName("results")
    List<Movie> movies;

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }
}
