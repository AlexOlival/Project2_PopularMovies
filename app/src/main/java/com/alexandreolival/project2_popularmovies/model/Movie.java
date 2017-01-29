package com.alexandreolival.project2_popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alexandreolival.project2_popularmovies.network.NetworkUtil;

public class Movie implements Parcelable {

    private String posterPath;
    private String title;
    private String releaseDate;
    private String voteAverage;
    private String synopsis;

    public Movie(String posterPath, String title, String releaseDate, String voteAverage, String synopsis) {
        this.posterPath = posterPath;
        this.title = title;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.synopsis = synopsis;
    }

    public String getPosterUri() {
        return NetworkUtil.BASE_MOVIEDB_POSTER_URL + posterPath;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
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

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    @Override
    public String toString() {
        return "{ poster_uri: " + getPosterUri() +
                " title: " + getTitle() +
                " release_date: " + getReleaseDate() +
                " vote_average: " + getVoteAverage() +
                " synopsis: " + getSynopsis() + " }";
    }

    private Movie(Parcel in) {
        posterPath = in.readString();
        title = in.readString();
        releaseDate = in.readString();
        voteAverage = in.readString();
        synopsis = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(posterPath);
        dest.writeString(title);
        dest.writeString(releaseDate);
        dest.writeString(voteAverage);
        dest.writeString(synopsis);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

}
