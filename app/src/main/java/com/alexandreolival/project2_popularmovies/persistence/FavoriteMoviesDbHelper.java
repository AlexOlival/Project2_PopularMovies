package com.alexandreolival.project2_popularmovies.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoriteMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorite_movies.db";
    private static final int DATABASE_VERSION = 1;

    public FavoriteMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVORITE_MOVIES_TABLE = "CREATE TABLE " +
                FavoriteMoviesContract.MovieFavoriteEntry.TABLE_NAME + " (" +
                FavoriteMoviesContract.MovieFavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_MOVIE_ID + " TEXT NOT NULL, " +
                FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_POSTER + " TEXT NOT NULL, " +
                FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL, " +
                FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_VOTE_AVERAGE + " TEXT NOT NULL, " +
                FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_SYNOPSIS + " TEXT NOT NULL, " +
                FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_RELEASE_DATE + " TEXT NOT NULL " +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteMoviesContract.MovieFavoriteEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
