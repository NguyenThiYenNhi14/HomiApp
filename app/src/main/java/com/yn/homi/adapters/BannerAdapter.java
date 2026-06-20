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
import com.yn.homi.models.Banner;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Banner> banners;

    public BannerAdapter(List<Banner> banners) {
        this.banners = banners;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = banners.get(position);
        holder.tvTitle.setText(banner.getTitle());
        holder.tvSubtitle.setText(banner.getSubtitle());

        Glide.with(holder.itemView.getContext())
                .load(banner.getImageUrl())
                .placeholder(R.color.gray_light)
                .into(holder.ivBanner);
    }

    @Override
    public int getItemCount() {
        return banners != null ? banners.size() : 0;
    }

    public void updateData(List<Banner> newBanners) {
        this.banners = newBanners;
        notifyDataSetChanged();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        TextView tvTitle, tvSubtitle;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.iv_banner);
            tvTitle = itemView.findViewById(R.id.tv_banner_title);
            tvSubtitle = itemView.findViewById(R.id.tv_banner_subtitle);
        }
    }
}
