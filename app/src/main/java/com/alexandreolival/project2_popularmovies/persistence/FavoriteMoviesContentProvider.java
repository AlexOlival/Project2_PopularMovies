package com.alexandreolival.project2_popularmovies.persistence;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import static com.alexandreolival.project2_popularmovies.persistence.FavoriteMoviesContract.MovieFavoriteEntry.TABLE_NAME;

public class FavoriteMoviesContentProvider extends ContentProvider {

    public static final int FAVORITE_MOVIES = 100;
    public static final int FAVORITE_MOVIE_BY_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(FavoriteMoviesContract.AUTHORITY, FavoriteMoviesContract.PATH_FAVORITES,
                FAVORITE_MOVIES);

        uriMatcher.addURI(FavoriteMoviesContract.AUTHORITY,
                FavoriteMoviesContract.PATH_FAVORITES + "/*",
                FAVORITE_MOVIE_BY_ID);

        return uriMatcher;
    }

    private FavoriteMoviesDbHelper mFavoriteMoviesDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mFavoriteMoviesDbHelper = new FavoriteMoviesDbHelper(context);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SQLiteDatabase database = mFavoriteMoviesDbHelper.getReadableDatabase();

        Cursor returnCursor;

        switch (sUriMatcher.match(uri)) {
            case FAVORITE_MOVIES:
                returnCursor = database.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case FAVORITE_MOVIE_BY_ID:
                String id = uri.getPathSegments().get(1);
                String idWhereClause =
                        FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_MOVIE_ID + "=?";
                String[] whereClauseArguments = new String[] {id};

                returnCursor = database.query(TABLE_NAME,
                        projection,
                        idWhereClause,
                        whereClauseArguments,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Invalid uri: " + uri);
        }

        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase database = mFavoriteMoviesDbHelper.getWritableDatabase();

        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case FAVORITE_MOVIES:
                long id = database.insert(TABLE_NAME,
                        null,
                        contentValues);

                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(
                            FavoriteMoviesContract.MovieFavoriteEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Invalid uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        final SQLiteDatabase database = mFavoriteMoviesDbHelper.getWritableDatabase();

        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case FAVORITE_MOVIE_BY_ID:
                String movieId = uri.getPathSegments().get(1);
                rowsDeleted = database.delete(TABLE_NAME,
                        FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_MOVIE_ID + "=?",
                        new String[]{movieId});
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        // We don't need to update favorite records
        throw new UnsupportedOperationException("Updating isn't allowed!");
    }
}
