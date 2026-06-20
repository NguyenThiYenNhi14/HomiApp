package com.yn.homi.adapters;

import android.graphics.Outline;
import android.graphics.Path;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yn.homi.R;
import com.yn.homi.models.QuickTab;

import java.util.List;

public class QuickTabAdapter extends RecyclerView.Adapter<QuickTabAdapter.ViewHolder> {

    private List<QuickTab> quickTabs;
    private OnQuickTabClickListener listener;

    public interface OnQuickTabClickListener {
        void onQuickTabClick(QuickTab tab);
    }

    public QuickTabAdapter(List<QuickTab> quickTabs) {
        this.quickTabs = quickTabs;
    }

    public void setOnQuickTabClickListener(OnQuickTabClickListener listener) {
        this.listener = listener;
    }

    public void setQuickTabs(List<QuickTab> quickTabs) {
        this.quickTabs = quickTabs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quick_tab, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuickTab tab = quickTabs.get(position);
        holder.tvName.setText(tab.getName());

        // Setup clipping for the house shape
        holder.ivIcon.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int width = view.getWidth();
                int height = view.getHeight();
                if (width <= 0 || height <= 0) return;

                Path path = new Path();
                // Match the path in bg_house_shape.xml: M 40,0 L 80,30 L 80,100 L 0,100 L 0,30 Z
                // Scaled to view dimensions (80x100 viewport)
                path.moveTo(width * 40f / 80f, 0);
                path.lineTo(width, height * 30f / 100f);
                path.lineTo(width, height);
                path.lineTo(0, height);
                path.lineTo(0, height * 30f / 100f);
                path.close();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    outline.setPath(path);
                } else {
                    outline.setConvexPath(path);
                }
            }
        });
        holder.ivIcon.setClipToOutline(true);

        if ("sales".equals(tab.getSlug())) {
            holder.ivBackground.setImageResource(R.drawable.bg_house_shape_yellow);
            holder.ivIcon.setVisibility(View.GONE);
            holder.tvSaleLabel.setVisibility(View.VISIBLE);
        } else {
            holder.ivBackground.setImageResource(R.drawable.bg_house_shape);
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.tvSaleLabel.setVisibility(View.GONE);
            
            Glide.with(holder.itemView.getContext())
                    .load(tab.getImageUrl())
                    .centerCrop()
                    .into(holder.ivIcon);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuickTabClick(tab);
            }
        });
    }

    @Override
    public int getItemCount() {
        return quickTabs != null ? quickTabs.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBackground;
        ImageView ivIcon;
        TextView tvName;
        TextView tvSaleLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBackground = itemView.findViewById(R.id.iv_background);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvName = itemView.findViewById(R.id.tv_name);
            tvSaleLabel = itemView.findViewById(R.id.tv_sale_label);
        }
    }
}
