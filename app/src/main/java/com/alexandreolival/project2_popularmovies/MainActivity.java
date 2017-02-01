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
import com.alexandreolival.project2_popularmovies.model.Review;
import com.alexandreolival.project2_popularmovies.model.Trailer;
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

    // I learned that you can call [Activity].class.getSimpleName()
    // will keep in mind in future!
    private static final String TAG = "MainActivity";

    protected MovieAdapter mMovieAdapter;

    private static final int SORTED_BY_POPULARITY = 1;
    private static final int SORTED_BY_RATING = 2;
    private static final int SHOWING_FAVORITES = 3;

    private static final int MOVIE_DB_LOADER_ID = 22;
    private static final int FAVORITE_DB_LOADER_ID = 23;
    private static final int TRAILERS_REVIEWS_LOADER_ID = 24;
    private static final String MOVIE_DB_QUERY_URL_EXTRA = "MOVIE_DB_QUERY_URL";
    private static final String MOVIE_DB_URL_EXTRA = "MOVIE_DB_URL";

    private static final String KEY_SORTING_ORDER = "sorting_order";
    private static final String KEY_MOVIES = "movies";

    private Button mButtonRetry;
    private TextView mTextViewNoInternetConnection;
    protected ProgressBar mProgressBarLoadingMovies;
    private RecyclerView mRecyclerView;

    protected int mCurrentSortingOrder;

    // So the trailers & reviews loader knows which object it's referring too
    protected Movie mClickedMovie;

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
            mProgressBarLoadingMovies.setVisibility(View.GONE);
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {}
    };

    private LoaderManager.LoaderCallbacks<Cursor> mFavoriteMoviesDatabaseLoaderListener
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<Cursor>(getBaseContext()) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    mProgressBarLoadingMovies.setVisibility(View.VISIBLE);
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
                // There are no favorites! If there's network fallback to sorting by popularity
                if (isConnectedToNetwork()) {
                    loadMoviesSortedByPopularity();
                } else {
                    showNoInternetConnectionViews();
                }
            } else {
                // We have favorites!
                if (!isConnectedToNetwork()) {
                    // Irrelevant if we have network. This pulls from the local persistence
                    hideNoInternetConnectionViews();
                }
                mMovieAdapter.setMovieList(movieArrayList);
            }

            mProgressBarLoadingMovies.setVisibility(View.GONE);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    };

    private LoaderManager.LoaderCallbacks<String> mTrailersAndReviewsLoaderListener
            = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader<String> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<String>(getBaseContext()) {

                String jsonResponse;

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
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
                    String searchQueryUrlString = args.getString(MOVIE_DB_URL_EXTRA);
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
            ArrayList<Review> reviews = new ArrayList<>();
            ArrayList<Trailer> trailers = new ArrayList<>();
            try {
                JSONObject jsonResponse = new JSONObject(data);
                JSONArray jsonTrailersArray =
                        jsonResponse.getJSONObject("videos").getJSONArray("results");
                JSONArray jsonReviewsArray =
                        jsonResponse.getJSONObject("reviews").getJSONArray("results");
                //Log.d(TAG, "jsonTrailersArray" + jsonTrailersArray);
                //Log.d(TAG, "jsonReviewsArray: " + jsonReviewsArray);

                JSONObject review;
                for (int i = 0, length = jsonReviewsArray.length(); i < length; i++) {
                    review = jsonReviewsArray.getJSONObject(i);
                    reviews.add(
                            new Review(
                                    review.getString("author"),
                                    review.getString("content")
                            )
                    );
                }

                JSONObject trailer;
                for (int i = 0, length = jsonTrailersArray.length(); i < length; i++) {
                    trailer = jsonTrailersArray.getJSONObject(i);
                    trailers.add(
                            new Trailer(
                                    trailer.getString("name"),
                                    trailer.getString("key")
                            )
                    );
                }

                mClickedMovie.setReviews(reviews);
                mClickedMovie.setTrailers(trailers);

                //Log.d(TAG, "Movie reviews: " + mMovie.getReviews());
                //Log.d(TAG, "Movie trailers: " + mMovie.getTrailers());

                Log.d(TAG, "Got " + mClickedMovie.getTrailers().size() + " trailers and " +
                        mClickedMovie.getReviews().size() + " reviews for " + mClickedMovie.getTitle());


                startActivity(DetailActivity.getIntent(MainActivity.this, mClickedMovie));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBarLoadingMovies = (ProgressBar) findViewById(R.id.progress_bar_loading_movies);

        mTextViewNoInternetConnection = (TextView) findViewById(R.id.text_view_no_internet_connection);
        mButtonRetry = (Button) findViewById(R.id.button_retry_loading_movies);
        mButtonRetry.setOnClickListener(this);

        GridLayoutManager gridLayoutManager;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(this, 2);
        } else {
            gridLayoutManager = new GridLayoutManager(this, 4);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_movies);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        if (savedInstanceState == null) {
            // First load
            if (isConnectedToNetwork()) {
                loadMoviesSortedByPopularity();
            } else {
                showNoInternetConnectionViews();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Sometimes it shows the progress bar again after resuming from DetailActivity...
        // I'm not entirely sure why. I managed to reproduce the bug in an old 4.1 phone because
        // the RecyclerView has padding between the posters in that smaller screen
        mProgressBarLoadingMovies.setVisibility(View.GONE);

        // Check if there was any changes on the favorites database after resuming from
        // DetailActivity, like if the user removed a movie from the favorites. A bit messy
        // because I'm avoiding to use another adapter with a cursor just for the favorites
        // but it's working fine so far. DRY right?

        if (mCurrentSortingOrder == SHOWING_FAVORITES) {
            loadFavoriteMovies();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SORTING_ORDER, mCurrentSortingOrder);
        if (mMovieAdapter.getMovieList() != null) {
            outState.putParcelableArrayList(KEY_MOVIES, mMovieAdapter.getMovieList());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "Restoring instance state");
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentSortingOrder = savedInstanceState.getInt(KEY_SORTING_ORDER);
        if (savedInstanceState.containsKey(KEY_MOVIES)) {
            mMovieAdapter.setMovieList(savedInstanceState.<Movie>getParcelableArrayList(KEY_MOVIES));
        }
    }

    protected void loadMoviesSortedByPopularity() {
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

    private void loadFavoriteMovies() {
        mCurrentSortingOrder = SHOWING_FAVORITES;
        getSupportLoaderManager().restartLoader(FAVORITE_DB_LOADER_ID, null,
                mFavoriteMoviesDatabaseLoaderListener);
    }

    private void loadMovieReviewsAndTrailers() {
        Uri builtUri = Uri.parse(NetworkUtil.BASE_MOVIEDB_METADATA_URL).buildUpon()
                .appendPath(mClickedMovie.getMovieId())
                .appendQueryParameter(NetworkUtil.PARAMETER_API_KEY, NetworkUtil.API_KEY)
                .appendQueryParameter(NetworkUtil.PARAMETER_APPEND_MOVIE_DETAILS, NetworkUtil.MOVIE_DETAILS)
                .build();

        //Log.d(TAG, builtUri.toString());

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Bundle queryBundle = new Bundle();
        if (url != null) {
            queryBundle.putString(MOVIE_DB_URL_EXTRA, url.toString());
        }

        getSupportLoaderManager().restartLoader(TRAILERS_REVIEWS_LOADER_ID, queryBundle,
                mTrailersAndReviewsLoaderListener);
    }

    @Override
    public void onListItemClicked(Movie clickedMovieItem, View view) {
        Log.d(TAG, "Clicked movie with the following data: " + clickedMovieItem.toString());
        Log.d(TAG, "Starting DetailActivity");

        mClickedMovie = clickedMovieItem;

        if (isInFavoriteDatabase(mClickedMovie)) {
            // Let DetailActivity know this is a favorite movie
            mClickedMovie.setFavorite(true);
        }

        // Prefetch the trailers and movies lazy load style
        if (isConnectedToNetwork()) {
            // we have internet
            if (clickedMovieItem.getTrailers() == null || clickedMovieItem.getReviews() == null) {
                // no trailers or reviews, fetch and launch!
                loadMovieReviewsAndTrailers();
            } else {
                // we already have trailers or reviews, launch!
                startActivity(DetailActivity.getIntent(this, mClickedMovie));
            }
        } else if (!isConnectedToNetwork()) {
            // no internet
            if (clickedMovieItem.getTrailers() != null || clickedMovieItem.getReviews() != null) {
                // if we have trailers or reviews, all good
                startActivity(DetailActivity.getIntent(this, mClickedMovie));
            } else {
                // else we should warn the user
                Toast.makeText(this, R.string.toast_error_no_internet_trailers_reviews,
                        Toast.LENGTH_SHORT).show();
                startActivity(DetailActivity.getIntent(this, mClickedMovie));
            }
        }

        // I probably could simplify the if statement but... I'm too scared
        // http://s2.quickmeme.com/img/b4/b43f97387755436edb3fbad990afa7a13e51204aaf7cfb6e9cbfdd1d9d10f8ac.jpg

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
            // We assume it is not...
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
            case R.id.sort_most_popular:
                if (isConnectedToNetwork()) {
                    loadMoviesSortedByPopularity();
                } else {
                    showNoInternetConnectionViews();
                }
                return true;

            case R.id.sort_highest_rated:
                if (isConnectedToNetwork()) {
                    loadMoviesSortedByRatings();
                } else {
                    showNoInternetConnectionViews();
                }
                return true;

            case R.id.show_favorites:
                loadFavoriteMovies();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    boolean isConnectedToNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_retry_loading_movies:
                if (isConnectedToNetwork()) {
                    hideNoInternetConnectionViews();
                    switch (mCurrentSortingOrder) {
                        case SORTED_BY_POPULARITY:
                            loadMoviesSortedByPopularity();
                            break;

                        case SORTED_BY_RATING:
                            loadMoviesSortedByRatings();
                            break;

                        default:
                            loadMoviesSortedByPopularity();
                    }
                } else {
                    Toast.makeText(this, getString(com.alexandreolival.project2_popularmovies.R.string.toast_internet_connectivity_error),
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

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
