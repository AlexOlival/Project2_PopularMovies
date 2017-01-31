package com.alexandreolival.project2_popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alexandreolival.project2_popularmovies.model.Movie;
import com.alexandreolival.project2_popularmovies.model.Review;
import com.alexandreolival.project2_popularmovies.model.Trailer;
import com.alexandreolival.project2_popularmovies.network.NetworkUtil;
import com.alexandreolival.project2_popularmovies.persistence.FavoriteMoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {

    private static final String MOVIE_OBJECT_EXTRA = "movie_object";
    private static final String TAG = "DetailActivity";

    private static final int MOVIE_DB_DETAILS_LOADER_ID = 23;
    private static final String MOVIE_DB_URL_EXTRA = "MOVIE_DB_URL";
    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.alexandreolival.project2_popularmovies.R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent().hasExtra(MOVIE_OBJECT_EXTRA)) {
            Log.d(TAG, "Got movie from intent: " + getIntent().getParcelableExtra(MOVIE_OBJECT_EXTRA).toString());
            mMovie = getIntent().getParcelableExtra(MOVIE_OBJECT_EXTRA);

            // For the sake of speed I did not refactor this using Butterknife. I did appreciate the suggestion
            // and will use it in future projects!

            TextView textViewTitle = (TextView) findViewById(com.alexandreolival.project2_popularmovies.R.id.text_view_movie_title);
            TextView textViewSynopsis = (TextView) findViewById(com.alexandreolival.project2_popularmovies.R.id.text_view_movie_synopsis);
            TextView textViewRating = (TextView) findViewById(com.alexandreolival.project2_popularmovies.R.id.text_view_movie_rating);
            TextView textViewReleaseDate = (TextView) findViewById(com.alexandreolival.project2_popularmovies.R.id.text_view_movie_release_date);
            ImageView imageViewPoster = (ImageView) findViewById(com.alexandreolival.project2_popularmovies.R.id.image_view_movie_detail_poster);

            Picasso.with(this).load(mMovie.getPosterUri()).into(imageViewPoster);
            textViewTitle.setText(mMovie.getTitle());
            textViewSynopsis.setText(mMovie.getSynopsis());
            String score = mMovie.getVoteAverage() + getString(com.alexandreolival.project2_popularmovies.R.string.vote_average_maximum_score);
            textViewRating.setText(score);
            textViewReleaseDate.setText(mMovie.getReleaseDate());

            loadMovieTrailersAndReviews(mMovie.getMovieId());
        } else {
            Toast.makeText(this, getString(com.alexandreolival.project2_popularmovies.R.string.toast_movie_detail_error), Toast.LENGTH_SHORT).show();
        }

    }

    private void loadMovieTrailersAndReviews(String movieId) {
        Uri builtUri = Uri.parse(NetworkUtil.BASE_MOVIEDB_METADATA_URL).buildUpon()
                .appendPath(movieId)
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

        getSupportLoaderManager().restartLoader(MOVIE_DB_DETAILS_LOADER_ID, queryBundle, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.favorite_menu, menu);
        initializeFavoriteButton(menu);
        return true;
    }

    private void initializeFavoriteButton(Menu menu) {
        if (mMovie.isFavorite()) {
            menu.getItem(0).setIcon(R.drawable.ic_favorite);
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_not_favorite);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.toggle_favorite:
                ContentValues contentValues;
                if (mMovie.isFavorite()) {
                    int deletedRows = getContentResolver().delete(
                            FavoriteMoviesContract.MovieFavoriteEntry.CONTENT_URI
                                    .buildUpon().appendPath(mMovie.getMovieId()).build(),
                            null, null);

                    if (deletedRows != 0) {
                        mMovie.setFavorite(false);
                        item.setIcon(R.drawable.ic_not_favorite);
                        Toast.makeText(getBaseContext(),
                                R.string.toast_removed_from_favorites, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getBaseContext(),
                                R.string.toast_error_removing_from_favorites,
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    contentValues = new ContentValues();
                    contentValues.put(
                            FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_MOVIE_ID,
                            mMovie.getMovieId()
                    );
                    contentValues.put(
                            FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_POSTER,
                            mMovie.getPosterPath()
                    );
                    contentValues.put(
                            FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_RELEASE_DATE,
                            mMovie.getReleaseDate()
                    );
                    contentValues.put(
                            FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_SYNOPSIS,
                            mMovie.getSynopsis()
                    );
                    contentValues.put(
                            FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_TITLE,
                            mMovie.getTitle()
                    );
                    contentValues.put(
                            FavoriteMoviesContract.MovieFavoriteEntry.COLUMN_NAME_VOTE_AVERAGE,
                            mMovie.getVoteAverage()
                    );

                    Uri uri = getContentResolver().insert(
                            FavoriteMoviesContract.MovieFavoriteEntry.CONTENT_URI, contentValues);

                    if (uri != null) {
                        mMovie.setFavorite(true);
                        item.setIcon(R.drawable.ic_favorite);
                        Toast.makeText(getBaseContext(),
                                R.string.toast_saved_to_favorites, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getBaseContext(),
                                R.string.toast_error_saving_to_favorites,
                                Toast.LENGTH_SHORT).show();
                    }

                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getIntent(Context context, Parcelable movie) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(MOVIE_OBJECT_EXTRA, movie);
        return intent;
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

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

            mMovie.setReviews(reviews);
            mMovie.setTrailers(trailers);

            //Log.d(TAG, "Movie reviews: " + mMovie.getReviews());
            //Log.d(TAG, "Movie trailers: " + mMovie.getTrailers());

            Log.d(TAG, "Got " + mMovie.getTrailers().size() + " trailers and " +
            mMovie.getReviews().size() + " reviews for " + mMovie.getTitle());

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
