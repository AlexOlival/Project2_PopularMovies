package com.alexandreolival.project2_popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alexandreolival.project2_popularmovies.R;
import com.alexandreolival.project2_popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final static String TAG = "MovieAdapter";

    private List<Movie> mMovieList;

    public interface ListItemClickedListener {
        void onListItemClicked(Movie clickedItem, View view);
    }

    private final ListItemClickedListener mListItemClickedListener;

    public MovieAdapter(ListItemClickedListener listItemClickedListener) {
        mListItemClickedListener = listItemClickedListener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutForItem = R.layout.movie_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutForItem, parent, false);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = mMovieList.get(position);
        Picasso.with(holder.moviePoster.getContext())
                .load(movie.getPosterUri())
                .into(holder.moviePoster);
    }

    @Override
    public int getItemCount() {
        if (mMovieList == null) {
            return 0;
        } else {
            return mMovieList.size();
        }
    }

    public void setMovieList(List<Movie> movieList) {
        Log.d(TAG, "Movie list updated");
        if (mMovieList != null) {
            mMovieList.clear();
        }
        this.mMovieList = movieList;
        notifyDataSetChanged();
    }

    public ArrayList<Movie> getMovieList() {
        if (mMovieList != null) {
            return new ArrayList<>(mMovieList);
        } else {
            return null;
        }
    }

    public void clearMovieList() {
        if (mMovieList != null) {
            mMovieList.clear();
        }
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

        ImageView moviePoster;

        MovieViewHolder(View itemView) {
            super(itemView);
            moviePoster = (ImageView) itemView.findViewById(R.id.image_view_movie_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListItemClickedListener.onListItemClicked(mMovieList.get(getAdapterPosition()), view);
        }
    }

}
