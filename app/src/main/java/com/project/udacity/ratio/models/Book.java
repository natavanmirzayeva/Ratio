package com.project.udacity.ratio.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Book implements Parcelable {

    @SerializedName("imageLinks")
    private ImageLinkBook posterPath;

    @SerializedName("title")
    private String title;

    @SerializedName("authors")
    private List<String> authors;

    @SerializedName("publishedDate")
    private String releaseDate;

    @SerializedName("language")
    private String language;

    @SerializedName("description")
    private String description;

    private String id;

    private double voteAverage;
    private int voteCount;
    private int voteScore;

    public Book() {
    }

    public Book(String title, ImageLinkBook posterPath, String description, double voteAverage, int voteCount, int voteScore,
                String releaseDate, String language, String id, List<String> authors) {
        this.title = title;
        this.posterPath = posterPath;
        this.description = description;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.voteScore = voteScore;
        this.releaseDate = releaseDate;
        this.language = language;
        this.id = id;
        this.authors = authors;

    }

    public Book(Parcel in) {
        this.title = in.readString();
        this.posterPath = in.readParcelable(Book.class.getClassLoader());
        this.description = in.readString();
        this.voteAverage = in.readDouble();
        this.voteCount = in.readInt();
        this.voteScore = in.readInt();
        this.releaseDate = in.readString();
        this.language = in.readString();
        this.id = in.readString();
        this.authors = new ArrayList<>();
        in.readList(authors, Book.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeParcelable(posterPath, flags);
        dest.writeString(description);
        dest.writeDouble(voteAverage);
        dest.writeInt(voteCount);
        dest.writeInt(voteScore);
        dest.writeString(releaseDate);
        dest.writeString(language);
        dest.writeString(id);
        dest.writeList(authors);
    }

    public static final Creator CREATOR = new Creator<Book>() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public ImageLinkBook getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(ImageLinkBook posterPath) {
        this.posterPath = posterPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
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

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
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