package com.project.udacity.ratio.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mehseti on 27.7.2018.
 */

public class ImageLinkBook implements Parcelable {
    @SerializedName("smallThumbnail")
    private String smallThumbnail;
    @SerializedName("thumbnail")
    private String thumbnail;

    public ImageLinkBook() {
    }

    public ImageLinkBook(Parcel in) {
        this.smallThumbnail = in.readString();
        this.thumbnail = in.readString();
    }

    public static final Creator<ImageLinkBook> CREATOR = new Creator<ImageLinkBook>() {
        @Override
        public ImageLinkBook createFromParcel(Parcel in) {
            return new ImageLinkBook(in);
        }

        @Override
        public ImageLinkBook[] newArray(int size) {
            return new ImageLinkBook[size];
        }
    };

    public String getSmallThumbnail() {
        return smallThumbnail;
    }

    public void setSmallThumbnail(String smallThumbnail) {
        this.smallThumbnail = smallThumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(smallThumbnail);
        dest.writeString(thumbnail);
    }
}