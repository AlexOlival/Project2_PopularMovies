package com.alexandreolival.project2_popularmovies.persistence;

import android.provider.BaseColumns;

public final class FavoriteMoviesContract {

    private FavoriteMoviesContract() {}

    public static class MovieFavoriteEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_NAME_MOVIE_ID = "movieId";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_POSTER = "poster";
        public static final String COLUMN_NAME_SYNOPSIS = "synopsis";
        public static final String COLUMN_NAME_VOTE_AVERAGE = "voteAverage";
        public static final String COLUMN_NAME_RELEASE_DATE = "releaseDate";
    }

}
