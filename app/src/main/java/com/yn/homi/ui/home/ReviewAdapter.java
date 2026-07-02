package com.yn.homi.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yn.homi.R;
import com.yn.homi.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void updateData(List<Review> newReviews) {
        this.reviews = newReviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        
        holder.tvUserName.setText(review.getReviewerName() != null ? review.getReviewerName() : "Anonymous");
        holder.rbRating.setRating(review.getRating());
        holder.tvDate.setText(review.getDate() != null ? review.getDate() : "");
        holder.tvContent.setText(review.getBody() != null ? review.getBody() : "");

        if (review.getTitle() != null && !review.getTitle().isEmpty()) {
            holder.tvTitle.setText(review.getTitle());
            holder.tvTitle.setVisibility(View.VISIBLE);
        } else {
            holder.tvTitle.setVisibility(View.GONE);
        }

        if (review.getImageUrl() != null && !review.getImageUrl().isEmpty()) {
            holder.ivReviewImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(review.getImageUrl())
                    .into(holder.ivReviewImage);
        } else {
            holder.ivReviewImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvDate, tvContent, tvTitle;
        RatingBar rbRating;
        ImageView ivReviewImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvDate = itemView.findViewById(R.id.tv_review_date);
            tvContent = itemView.findViewById(R.id.tv_review_content);
            tvTitle = itemView.findViewById(R.id.tv_review_title);
            rbRating = itemView.findViewById(R.id.rb_review_rating);
            ivReviewImage = itemView.findViewById(R.id.iv_review_image);
        }
    }
}
