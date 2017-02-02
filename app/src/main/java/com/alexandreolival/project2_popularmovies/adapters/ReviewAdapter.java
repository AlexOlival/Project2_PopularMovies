package com.alexandreolival.project2_popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexandreolival.project2_popularmovies.R;
import com.alexandreolival.project2_popularmovies.model.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final static String TAG = "ReviewAdapter";

    private List<Review> mReviewList;

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutForItem = R.layout.review_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutForItem, parent, false);

        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Review review = mReviewList.get(position);
        holder.reviewContent.setText(review.getContent());
        holder.reviewAuthor.setText(review.getAuthor());
    }

    @Override
    public int getItemCount() {
        if (mReviewList == null) {
            return 0;
        } else {
            return mReviewList.size();
        }
    }

    public void setReviewList(List<Review> reviewList) {
        Log.d(TAG, "Trailer list updated");
        if (mReviewList != null) {
            mReviewList.clear();
        }
        this.mReviewList = reviewList;
        notifyDataSetChanged();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView reviewContent;
        TextView reviewAuthor;

        ReviewViewHolder(View itemView) {
            super(itemView);
            reviewContent = (TextView) itemView.findViewById(R.id.text_view_review_content);
            reviewAuthor = (TextView) itemView.findViewById(R.id.text_view_review_author);
        }

    }

}
