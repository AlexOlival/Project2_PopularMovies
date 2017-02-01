package com.alexandreolival.project2_popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alexandreolival.project2_popularmovies.R;
import com.alexandreolival.project2_popularmovies.model.Trailer;

import java.util.List;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private final static String TAG = "TrailerAdapter";

    private List<Trailer> mTrailerList;

    public interface ShareButtonClickedListener {
        void onShareButtonClicked(Trailer clickedItem, View view);
    }

    public interface WatchButtonClickedListener {
        void onWatchButtonClicked(Trailer clickedItem, View view);
    }

    private final ShareButtonClickedListener mShareButtonClickedListener;
    private final WatchButtonClickedListener mWatchButtonClickedListener;

    public TrailerAdapter(ShareButtonClickedListener shareButtonClickedListener,
                          WatchButtonClickedListener watchButtonClickedListener) {
        mShareButtonClickedListener = shareButtonClickedListener;
        mWatchButtonClickedListener = watchButtonClickedListener;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutForItem = R.layout.trailer_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutForItem, parent, false);

        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        Trailer trailer = mTrailerList.get(position);
        holder.trailerTitle.setText(trailer.getTitle());
    }

    @Override
    public int getItemCount() {
        if (mTrailerList == null) {
            return 0;
        } else {
            return mTrailerList.size();
        }
    }

    public void setTrailerList(List<Trailer> trailerList) {
        Log.d(TAG, "Trailer list updated");
        if (mTrailerList != null) {
            mTrailerList.clear();
        }
        this.mTrailerList = trailerList;
        notifyDataSetChanged();
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView trailerTitle;
        ImageButton shareButton;
        ImageButton watchButton;

        TrailerViewHolder(View itemView) {
            super(itemView);
            trailerTitle = (TextView) itemView.findViewById(R.id.text_view_trailer_title);
            shareButton = (ImageButton) itemView.findViewById(R.id.button_share_trailer);
            watchButton = (ImageButton) itemView.findViewById(R.id.button_watch_trailer);
            shareButton.setOnClickListener(this);
            watchButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_share_trailer:
                    mShareButtonClickedListener.onShareButtonClicked(
                            mTrailerList.get(getAdapterPosition()), view);
                    break;

                case R.id.button_watch_trailer:
                    mWatchButtonClickedListener.onWatchButtonClicked(
                            mTrailerList.get(getAdapterPosition()), view);
            }

        }
    }

}
