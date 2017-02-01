package com.alexandreolival.project2_popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alexandreolival.project2_popularmovies.network.NetworkUtil;

public class Trailer implements Parcelable {


    private String title;
    private String url;

    public Trailer(String title, String key) {
        this.title = title;
        this.url = NetworkUtil.BASE_YOUTUBE_VIDEO_URL + key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.url);
    }

    protected Trailer(Parcel in) {
        this.title = in.readString();
        this.url = in.readString();
    }

    public static final Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel source) {
            return new Trailer(source);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };
}
