package com.yn.homi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;
import com.yn.homi.models.Product;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Product.Review> reviews;

    public ReviewAdapter(List<Product.Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product.Review review = reviews.get(position);
        
        holder.tvUserName.setText(review.getReviewerName());
        holder.rbRating.setRating(review.getRating());
        holder.tvDate.setText(review.getDate());
        holder.tvContent.setText(review.getBody());

        if (review.getTitle() != null && !review.getTitle().isEmpty()) {
            holder.tvTitle.setText(review.getTitle());
            holder.tvTitle.setVisibility(View.VISIBLE);
        } else {
            holder.tvTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvDate, tvContent, tvTitle;
        RatingBar rbRating;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvDate = itemView.findViewById(R.id.tv_review_date);
            tvContent = itemView.findViewById(R.id.tv_review_content);
            tvTitle = itemView.findViewById(R.id.tv_review_title);
            rbRating = itemView.findViewById(R.id.rb_review_rating);
        }
    }
}
