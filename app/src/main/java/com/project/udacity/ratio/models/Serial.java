package com.project.udacity.ratio.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Serial implements Parcelable {

    @SerializedName("image")
    private ImageLinkSerial posterPath;

    @SerializedName("name")
    private String title;

    @SerializedName("genres")
    private List<String> genres;

    @SerializedName("premiered")
    private String releaseDate;

    @SerializedName("language")
    private String language;

    @SerializedName("summary")
    private String description;

    @SerializedName("id")
    private String id;

    private double voteAverage;
    private int voteCount;
    private int voteScore;


    public Serial() {
    }

    public Serial(String title, ImageLinkSerial posterPath, String description, double voteAverage,
                  int voteCount, int voteScore, String releaseDate, String language, String id,
                  List<String> genres) {
        this.title = title;
        this.posterPath = posterPath;
        this.description = description;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.language = language;
        this.genres = genres;
        this.voteCount = voteCount;
        this.voteScore = voteScore;
        this.genres = genres;
        this.id = id;
    }

    public Serial(Parcel in) {
        this.title = in.readString();
        this.posterPath = in.readParcelable(Serial.class.getClassLoader());
        this.description = in.readString();
        this.voteAverage = in.readDouble();
        this.voteCount = in.readInt();
        this.voteScore = in.readInt();
        this.releaseDate = in.readString();
        this.language = in.readString();
        genres = new ArrayList<>();
        in.readList(genres, Serial.class.getClassLoader());
        this.id = in.readString();

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
        dest.writeList(genres);
        dest.writeString(id);


    }

    public static final Creator CREATOR = new Creator<Serial>() {
        public Serial createFromParcel(Parcel in) {
            return new Serial(in);
        }

        public Serial[] newArray(int size) {
            return new Serial[size];
        }
    };

    public ImageLinkSerial getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(ImageLinkSerial posterPath) {
        this.posterPath = posterPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
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