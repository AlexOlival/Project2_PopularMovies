package com.alexandreolival.project2_popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alexandreolival.project2_popularmovies.adapters.TrailerAdapter;
import com.alexandreolival.project2_popularmovies.model.Movie;
import com.alexandreolival.project2_popularmovies.model.Trailer;
import com.alexandreolival.project2_popularmovies.persistence.FavoriteMoviesContract;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity implements
        TrailerAdapter.ShareButtonClickedListener, TrailerAdapter.WatchButtonClickedListener {

    private static final String MOVIE_OBJECT_EXTRA = "movie_object";
    private static final String TAG = "DetailActivity";
    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.alexandreolival.project2_popularmovies.R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent().hasExtra(MOVIE_OBJECT_EXTRA)) {
            Log.d(TAG, "Got movie from intent: " +
                    getIntent().getParcelableExtra(MOVIE_OBJECT_EXTRA).toString());
            mMovie = getIntent().getParcelableExtra(MOVIE_OBJECT_EXTRA);

            // For the sake of speed I did not refactor this using butter knife.
            // I did appreciate the suggestion and will use it in future projects!
            TextView textViewTitle = (TextView) findViewById(R.id.text_view_movie_title);
            TextView textViewSynopsis = (TextView) findViewById(R.id.text_view_movie_synopsis);
            TextView textViewRating = (TextView) findViewById(R.id.text_view_movie_rating);
            TextView textViewReleaseDate = (TextView) findViewById(R.id.text_view_movie_release_date);
            ImageView imageViewPoster = (ImageView) findViewById(R.id.image_view_movie_detail_poster);

            Picasso.with(this).load(mMovie.getPosterUri()).into(imageViewPoster);
            textViewTitle.setText(mMovie.getTitle());
            textViewSynopsis.setText(mMovie.getSynopsis());
            String score = mMovie.getVoteAverage() + getString(com.alexandreolival.project2_popularmovies.R.string.vote_average_maximum_score);
            textViewRating.setText(score);
            textViewReleaseDate.setText(mMovie.getReleaseDate());

            if (!mMovie.getTrailers().isEmpty() && mMovie.getTrailers() != null) {
                RecyclerView trailersRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_trailers);
                trailersRecyclerView.setHasFixedSize(true);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                trailersRecyclerView.setLayoutManager(linearLayoutManager);

                TrailerAdapter trailerAdapter = new TrailerAdapter(this, this);
                trailerAdapter.setTrailerList(mMovie.getTrailers());
                trailersRecyclerView.setAdapter(trailerAdapter);
                trailersRecyclerView.setNestedScrollingEnabled(false);
            } else if (mMovie.getTrailers().isEmpty() || mMovie.getTrailers() == null) {
                findViewById(R.id.recycler_view_trailers).setVisibility(View.GONE);
                findViewById(R.id.label_movie_trailers).setVisibility(View.GONE);
            }

        } else {
            Toast.makeText(this, getString(R.string.toast_movie_detail_error),
                    Toast.LENGTH_SHORT).show();
        }

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
                if (mMovie.isFavorite()) {
                    removeMovieFromFavorites(item);
                } else {
                    addMovieToFavorites(item);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeMovieFromFavorites(MenuItem item) {
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
    }

    private void addMovieToFavorites(MenuItem item) {
        ContentValues contentValues;
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

    public static Intent getIntent(Context context, Parcelable movie) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(MOVIE_OBJECT_EXTRA, movie);
        return intent;
    }

    @Override
    public void onShareButtonClicked(Trailer clickedItem, View view) {
        Toast.makeText(this, "Share " + clickedItem.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWatchButtonClicked(Trailer clickedItem, View view) {
        Toast.makeText(this, "Watch " + clickedItem.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
