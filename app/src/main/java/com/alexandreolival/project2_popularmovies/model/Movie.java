package com.alexandreolival.project2_popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.alexandreolival.project2_popularmovies.network.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

public class Movie implements Parcelable {

    private String movieId;
    private String posterPath;
    private String title;
    private String releaseDate;
    private String voteAverage;
    private String synopsis;
    private List<Review> reviews;
    private List<Trailer> trailers;
    private boolean isFavorite;

    public Movie(String id, String posterPath, String title, String releaseDate, String voteAverage,
                 String synopsis) {
        this.movieId = id;
        this.posterPath = posterPath;
        this.title = title;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.synopsis = synopsis;
    }

    public Movie(String id, String posterPath, String title, String releaseDate, String voteAverage,
                 String synopsis, boolean isFavorite) {
        this.movieId = id;
        this.posterPath = posterPath;
        this.title = title;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.synopsis = synopsis;
        this.isFavorite = isFavorite;
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

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String id) {
        this.movieId = id;
    }

    public List<Trailer> getTrailers() {
        return trailers;
    }

    public void setTrailers(List<Trailer> trailerUrls) {
        this.trailers = trailerUrls;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    @Override
    public String toString() {
        return "{ id: " + getMovieId() +
                " favorite: " + isFavorite() +
                " poster_uri: " + getPosterUri() +
                " title: " + getTitle() +
                " release_date: " + getReleaseDate() +
                " vote_average: " + getVoteAverage() +
                " synopsis: " + getSynopsis() + " }";
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.movieId);
        dest.writeString(this.posterPath);
        dest.writeString(this.title);
        dest.writeString(this.releaseDate);
        dest.writeString(this.voteAverage);
        dest.writeString(this.synopsis);
        dest.writeList(this.reviews);
        dest.writeList(this.trailers);
        dest.writeByte(this.isFavorite ? (byte) 1 : (byte) 0);
    }

    protected Movie(Parcel in) {
        this.movieId = in.readString();
        this.posterPath = in.readString();
        this.title = in.readString();
        this.releaseDate = in.readString();
        this.voteAverage = in.readString();
        this.synopsis = in.readString();
        this.reviews = new ArrayList<Review>();
        in.readList(this.reviews, Review.class.getClassLoader());
        this.trailers = new ArrayList<Trailer>();
        in.readList(this.trailers, Trailer.class.getClassLoader());
        this.isFavorite = in.readByte() != 0;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
