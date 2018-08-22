package com.project.udacity.ratio.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ImageLinkSerial implements Parcelable {
    @SerializedName("medium")
    private String smallThumbnail;

    @SerializedName("original")
    private String thumbnail;

    public ImageLinkSerial() {
    }


    public ImageLinkSerial(Parcel in) {
        this.smallThumbnail = in.readString();
        this.thumbnail = in.readString();
    }

    public static final Creator<ImageLinkSerial> CREATOR = new Creator<ImageLinkSerial>() {
        @Override
        public ImageLinkSerial createFromParcel(Parcel in) {
            return new ImageLinkSerial(in);
        }

        @Override
        public ImageLinkSerial[] newArray(int size) {
            return new ImageLinkSerial[size];
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