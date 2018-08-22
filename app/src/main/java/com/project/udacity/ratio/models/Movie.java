package com.project.udacity.ratio.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Movie implements Parcelable {

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("title")
    private String title;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("original_language")
    private String language;

    @SerializedName("overview")
    private String description;

    @SerializedName("id")
    private String id;

    private int voteCount;
    private int voteScore;
    private double voteAverage;

    public Movie() {
    }

    public Movie(String posterPath,
                 String backdropPath, String title, String releaseDate,
                 String language, double voteAverage, int voteCount, int voteScore,
                 String description, String id) {

        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.title = title;
        this.releaseDate = releaseDate;
        this.language = language;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.voteScore = voteScore;
        this.description = description;
        this.id = id;
    }

    public Movie(Parcel in) {

        this.posterPath = in.readString();
        this.backdropPath = in.readString();
        this.title = in.readString();
        this.releaseDate = in.readString();
        this.language = in.readString();
        this.voteAverage = in.readDouble();
        this.description = in.readString();
        this.id = in.readString();
        this.voteCount = in.readInt();
        this.voteScore = in.readInt();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(posterPath);
        dest.writeString(backdropPath);
        dest.writeString(title);
        dest.writeString(releaseDate);
        dest.writeString(language);
        dest.writeDouble(voteAverage);
        dest.writeString(description);
        dest.writeString(id);
        dest.writeInt(voteCount);
        dest.writeInt(voteScore);
    }

    public static final Creator CREATOR = new Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public int getVoteScore() {
        return voteScore;
    }

    public void setVoteScore(int voteScore) {
        this.voteScore = voteScore;
    }


}