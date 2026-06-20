package com.yn.homi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;
import com.yn.homi.models.ShopSection;

import java.util.List;

public class ShopSectionAdapter extends RecyclerView.Adapter<ShopSectionAdapter.ViewHolder> {

    private final List<ShopSection> sections;
    private final RoomSubCategoryAdapter.OnSubCategoryClickListener listener;

    public ShopSectionAdapter(List<ShopSection> sections, RoomSubCategoryAdapter.OnSubCategoryClickListener listener) {
        this.sections = sections;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopSection section = sections.get(position);
        holder.bind(section, listener);
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSectionTitle;
        private final RecyclerView rvSubcategories;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.tv_section_title);
            rvSubcategories = itemView.findViewById(R.id.rv_subcategories);
        }

        public void bind(ShopSection section, RoomSubCategoryAdapter.OnSubCategoryClickListener listener) {
            tvSectionTitle.setText(section.getCategory().getName());
            
            rvSubcategories.setLayoutManager(new GridLayoutManager(itemView.getContext(), 4));
            RoomSubCategoryAdapter adapter = new RoomSubCategoryAdapter(section.getSubCategories(), listener);
            rvSubcategories.setAdapter(adapter);
            
            // Important for nested RecyclerViews inside NestedScrollView
            rvSubcategories.setNestedScrollingEnabled(false);
        }
    }
}
