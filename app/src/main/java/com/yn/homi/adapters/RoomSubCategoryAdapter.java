package com.yn.homi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.yn.homi.R;
import com.yn.homi.models.RoomSubCategory;

import java.util.List;

public class RoomSubCategoryAdapter extends RecyclerView.Adapter<RoomSubCategoryAdapter.ViewHolder> {

    private final List<RoomSubCategory> list;
    private final OnSubCategoryClickListener listener;

    public interface OnSubCategoryClickListener {
        void onClick(RoomSubCategory subCategory);
    }

    public RoomSubCategoryAdapter(List<RoomSubCategory> list, OnSubCategoryClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_subcategory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomSubCategory item = list.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView ivSubCategory;
        private final TextView tvSubCategoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSubCategory = itemView.findViewById(R.id.iv_subcategory);
            tvSubCategoryName = itemView.findViewById(R.id.tv_subcategory_name);
        }

        public void bind(RoomSubCategory item, OnSubCategoryClickListener listener) {
            tvSubCategoryName.setText(item.getName());

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.color.gray_light)
                    .into(ivSubCategory);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(item);
                }
            });
        }
    }
}
