package com.yn.homi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.yn.homi.R;

import java.util.List;

public class IdeaCategoryAdapter extends RecyclerView.Adapter<IdeaCategoryAdapter.CategoryViewHolder> {

    private List<String> categories;
    private int selectedPosition = 0;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category, int position);
    }

    public IdeaCategoryAdapter(List<String> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_idea_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvName.setText(category);
        
        // Highlight selected category
        if (selectedPosition == position) {
            holder.tvName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.orange));
            holder.tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            holder.tvName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.tvName.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onCategoryClick(category, selectedPosition);
            }
        });
    }

    public String getSelectedCategory() {
        if (categories != null && selectedPosition >= 0 && selectedPosition < categories.size()) {
            return categories.get(selectedPosition);
        }
        return "All";
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}
