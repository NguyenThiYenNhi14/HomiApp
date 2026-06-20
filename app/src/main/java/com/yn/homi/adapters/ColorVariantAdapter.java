package com.yn.homi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yn.homi.R;
import com.yn.homi.models.Product;

import java.util.List;

public class ColorVariantAdapter extends RecyclerView.Adapter<ColorVariantAdapter.ViewHolder> {

    private List<Product.ColorVariant> variants;
    private int selectedPosition = 0;
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(Product.ColorVariant variant);
    }

    public ColorVariantAdapter(List<Product.ColorVariant> variants, OnColorSelectedListener listener) {
        this.variants = variants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_variant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product.ColorVariant variant = variants.get(position);
        
        holder.tvVariantName.setText(variant.getName());

        if (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(variant.getImageUrl())
                    .placeholder(R.color.gray_light)
                    .centerCrop()
                    .into(holder.ivVariantImage);
        } else {
            holder.ivVariantImage.setImageResource(R.color.gray_light);
        }

        holder.viewSelection.setVisibility(selectedPosition == position ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onColorSelected(variant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return variants != null ? variants.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVariantImage;
        TextView tvVariantName;
        View viewSelection;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVariantImage = itemView.findViewById(R.id.iv_variant_image);
            tvVariantName = itemView.findViewById(R.id.tv_variant_name);
            viewSelection = itemView.findViewById(R.id.view_selection);
        }
    }
}
