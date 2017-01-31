package com.alexandreolival.project2_popularmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alexandreolival.project2_popularmovies.adapters.MovieAdapter;
import com.alexandreolival.project2_popularmovies.model.Movie;
import com.alexandreolival.project2_popularmovies.network.NetworkUtil;
import com.alexandreolival.project2_popularmovies.persistence.FavoriteMoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        MovieAdapter.ListItemClickedListener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    protected MovieAdapter mMovieAdapter;

    private static final int SORTED_BY_POPULARITY = 1;
    private static final int SORTED_BY_RATING = 2;
    private static final int SHOWING_FAVORITES = 3;

    private static final int MOVIE_DB_LOADER_ID = 22;
    private static final int FAVORITE_DB_LOADER_ID = 23;
    private static final String MOVIE_DB_QUERY_URL_EXTRA = "MOVIE_DB_QUERY_URL";

    private Button mButtonRetry;
    private TextView mTextViewNoInternetConnection;
    protected ProgressBar mProgressBarLoadingMovies;
    private RecyclerView mRecyclerView;

    private int mCurrentSortingOrder;

    // Yikes... after writing this, I researched a way to separate these off the MainActivity.
    // but for the sake of speed, I'll leave them as inner classes.
    private LoaderManager.LoaderCallbacks<String> mMovieDatabaseLoaderListener
            = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader<String> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<String>(getBaseContext()) {

                String jsonResponse;

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    mProgressBarLoadingMovies.setVisibility(View.VISIBLE);
                    if (args == null) {
                        return;
                    }

                    if (jsonResponse != null) {
                        deliverResult(jsonResponse);
                    } else {
                        forceLoad();
                    }
                }

                @Override
                public String loadInBackground() {
                    String searchQueryUrlString = args.getString(MOVIE_DB_QUERY_URL_EXTRA);
                    if (searchQueryUrlString == null || searchQueryUrlString.isEmpty()) {
                        return null;
                    }

                    try {
                        URL url = new URL(searchQueryUrlString);
                        return NetworkUtil.getResponseFromHttpUrl(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public void deliverResult(String jsonResponse) {
                    this.jsonResponse = jsonResponse;
                    super.deliverResult(jsonResponse);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            mProgressBarLoadingMovies.setVisibility(View.GONE);
            List<Movie> movieArrayList = new ArrayList<>();
            try {
                JSONObject jsonResponse = new JSONObject(data);
                JSONArray jsonMoviesArray = jsonResponse.getJSONArray("results");
                //Log.d(TAG, "Request result: " + jsonResponse);
                //Log.d(TAG, "Request array length: " + jsonMoviesArray.length());
                JSONObject movieJson;
                //Log.d(TAG, jsonMoviesArray.getJSONObject(1).toString());
                for (int i = 0, length = jsonMoviesArray.length(); i < length; i++) {
                    movieJson = jsonMoviesArray.getJSONObject(i);
                    //Log.d(TAG, "JSON Object: " + movieJson);
                    movieArrayList.add(
                            new Movie(
                                    movieJson.getString("id"),
                                    movieJson.getString("poster_path"),
                                    movieJson.getString("title"),
                                    movieJson.getString("release_date"),
                                    movieJson.getString("vote_average"),
                                    movieJson.getString("overview")
                            )
                    );
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            mMovieAdapter.setMovieList(movieArrayList);
            if (movieArrayList.size() > 0) {
                hideNoInternetConnectionViews();
            } else {
                showNoInternetConnectionViews();
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {}
    };

    private LoaderManager.LoaderCallbacks<Cursor> mFavoriteMoviesDatabaseLoaderListener
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<Cursor>(getBaseContext()) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    forceLoad();
                }

                @Override
                public Cursor loadInBackground() {
                    try {
                        return getContentResolver().query(
                                FavoriteMoviesContract.MovieFavoriteEntry.CONTENT_URI,
                                null,
                                null,
                                null,
                                FavoriteMoviesContract.MovieFavoriteEntry._ID);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to load favourites data");
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public void deliverResult(Cursor data) {
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            List<Movie> movieArrayList = new ArrayList<>();
            while (cursor.moveToNext()) {
                movieArrayList.add(
                        new Movie(
                                cursor.getString(
                                        cursor.getColumnIndex(FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_MOVIE_ID)
                                ),
                                cursor.getString(
                                        cursor.getColumnIndex(FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_POSTER)
                                ),
                                cursor.getString(
                                        cursor.getColumnIndex(FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_TITLE)
                                ),
                                cursor.getString(
                                        cursor.getColumnIndex(FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_RELEASE_DATE)
                                ),
                                cursor.getString(
                                        cursor.getColumnIndex(FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_VOTE_AVERAGE)
                                ),
                                cursor.getString(
                                        cursor.getColumnIndex(FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_SYNOPSIS)
                                ),
                                true // It's a favorite! Use the alternative constructor
                        )
                );
            }

            cursor.close();

            if (movieArrayList.size() == 0) {
                Toast.makeText(MainActivity.this, R.string.toast_no_favorites,
                        Toast.LENGTH_SHORT).show();
            } else {
                for (Movie m : movieArrayList) {
                    Log.d(TAG, m.toString());
                }
                mMovieAdapter.setMovieList(movieArrayList);
            }

            mProgressBarLoadingMovies.setVisibility(View.GONE);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.alexandreolival.project2_popularmovies.R.layout.activity_main);

        mProgressBarLoadingMovies = (ProgressBar) findViewById(com.alexandreolival.project2_popularmovies.R.id.progress_bar_loading_movies);

        mTextViewNoInternetConnection = (TextView) findViewById(com.alexandreolival.project2_popularmovies.R.id.text_view_no_internet_connection);
        mButtonRetry = (Button) findViewById(com.alexandreolival.project2_popularmovies.R.id.button_retry_loading_movies);
        mButtonRetry.setOnClickListener(this);

        GridLayoutManager gridLayoutManager;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(this, 2);
        } else {
            gridLayoutManager = new GridLayoutManager(this, 4);
        }

        mRecyclerView = (RecyclerView) findViewById(com.alexandreolival.project2_popularmovies.R.id.recycler_view_movies);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        // First load
        if (checkInternetConnectivity()) {
            loadMoviesSortedByPopularity();
        } else {
            showNoInternetConnectionViews();
        }
    }

    @Override
    protected void onResume() {
        super.onRestart();
        // To fix an annoying bug; the ProgressBar would show when resuming from detail activity
        if (mCurrentSortingOrder == SORTED_BY_POPULARITY ||
                mCurrentSortingOrder == SORTED_BY_RATING) {
            if (checkInternetConnectivity()) {
                hideNoInternetConnectionViews();
            } else {
                showNoInternetConnectionViews();
            }
        }
    }

    private void loadMoviesSortedByPopularity() {
        mCurrentSortingOrder = SORTED_BY_POPULARITY;
        Uri builtUri = Uri.parse(NetworkUtil.BASE_MOVIEDB_METADATA_URL).buildUpon()
                .appendPath(NetworkUtil.PATH_SORT_BY_POPULAR)
                .appendQueryParameter(NetworkUtil.PARAMETER_API_KEY, NetworkUtil.API_KEY)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Bundle queryBundle = new Bundle();
        if (url != null) {
            queryBundle.putString(MOVIE_DB_QUERY_URL_EXTRA, url.toString());
        }

        getSupportLoaderManager().restartLoader(MOVIE_DB_LOADER_ID, queryBundle,
                mMovieDatabaseLoaderListener);
    }

    private void loadMoviesSortedByRatings() {
        mCurrentSortingOrder = SORTED_BY_RATING;
        Uri builtUri = Uri.parse(NetworkUtil.BASE_MOVIEDB_METADATA_URL).buildUpon()
                .appendPath(NetworkUtil.PATH_SORT_BY_RATING)
                .appendQueryParameter(NetworkUtil.PARAMETER_API_KEY, NetworkUtil.API_KEY)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Bundle queryBundle = new Bundle();
        if (url != null) {
            queryBundle.putString(MOVIE_DB_QUERY_URL_EXTRA, url.toString());
        }

        getSupportLoaderManager().restartLoader(MOVIE_DB_LOADER_ID, queryBundle,
                mMovieDatabaseLoaderListener);
    }

    @Override
    public void onListItemClicked(Movie clickedMovieItem, View view) {
        Log.d(TAG, "Clicked movie with the following data: " + clickedMovieItem.toString());
        Log.d(TAG, "Starting DetailActivity");

        if (isInFavoriteDatabase(clickedMovieItem)) {
            clickedMovieItem.setFavorite(true);
        }

        startActivity(DetailActivity.getIntent(this, clickedMovieItem));
    }

    private boolean isInFavoriteDatabase(Movie clickedMovieItem) {
        Cursor cursor = getContentResolver().query(
                FavoriteMoviesContract.MovieFavoriteEntry.CONTENT_URI
                        .buildUpon().appendPath(clickedMovieItem.getMovieId()).build(),
                null,
                FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_MOVIE_ID,
                new String[]{clickedMovieItem.getMovieId()},
                null);

        if (cursor != null) {
            cursor.close();
            // It means this movie is in the favorites database!
            return cursor.getCount() != 0;
        } else {
            // we assume it's not...
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.alexandreolival.project2_popularmovies.R.menu.sort_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.alexandreolival.project2_popularmovies.R.id.sort_most_popular:
                if (checkInternetConnectivity()) {
                    loadMoviesSortedByPopularity();
                } else {
                    showNoInternetConnectionViews();
                }
                return true;

            case com.alexandreolival.project2_popularmovies.R.id.sort_highest_rated:
                if (checkInternetConnectivity()) {
                    loadMoviesSortedByRatings();
                } else {
                    showNoInternetConnectionViews();
                }
                return true;

            case R.id.show_favorites:
                mCurrentSortingOrder = SHOWING_FAVORITES;
                // Irrelevant if we have network or not
                hideNoInternetConnectionViews();
                getSupportLoaderManager().restartLoader(FAVORITE_DB_LOADER_ID, null,
                        mFavoriteMoviesDatabaseLoaderListener);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkInternetConnectivity() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case com.alexandreolival.project2_popularmovies.R.id.button_retry_loading_movies:
                if (checkInternetConnectivity()) {
                    hideNoInternetConnectionViews();
                    loadMoviesSortedByPopularity();
                } else {
                    if (mProgressBarLoadingMovies.getVisibility() == View.VISIBLE) {
                        mProgressBarLoadingMovies.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, getString(com.alexandreolival.project2_popularmovies.R.string.toast_internet_connectivity_error),
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // Dearest reviwer,
    // I know this is horrible. I could probably refactor these into a cleaner method,
    // but I rather focus on the big features for project 2 now :p
    // as Han Solo put it: https://tinyurl.com/zst4bp4
    protected void hideNoInternetConnectionViews() {
        mButtonRetry.setVisibility(View.GONE);
        mTextViewNoInternetConnection.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    protected void showNoInternetConnectionViews() {
        mRecyclerView.setVisibility(View.GONE);
        mTextViewNoInternetConnection.setVisibility(View.VISIBLE);
        mButtonRetry.setVisibility(View.VISIBLE);
    }

}
