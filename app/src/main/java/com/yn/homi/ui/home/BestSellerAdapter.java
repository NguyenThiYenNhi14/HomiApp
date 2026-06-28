package com.yn.homi.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yn.homi.ui.shop.ProductDetailActivity;
import com.yn.homi.R;
import com.yn.homi.data.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BestSellerAdapter extends RecyclerView.Adapter<BestSellerAdapter.ViewHolder> {

    private List<Product> products = new ArrayList<>();

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_best_seller, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (products.isEmpty()) return;

        // Infinite loop logic: position % products.size()
        int realPosition = position % products.size();
        Product product = products.get(realPosition);

        String imageUrl = product.getThumbnailUrl();
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            imageUrl = product.getImageUrls().get(0);
        }

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .centerInside()
                .placeholder(R.color.gray_light)
                .error(R.color.gray_light)
                .into(holder.ivProduct);

        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(String.format(Locale.US, "$%.2f", product.getPrice()));

        // Smooth visibility/scale is now handled by PageTransformer in HomeActivity
        // We just ensure the details container is visible so alpha can be controlled
        holder.llInfo.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            holder.itemView.getContext().startActivity(intent);
        });

        if (holder.ivCart != null) {
            holder.ivCart.setOnClickListener(v -> {
                // TODO: Implement add to cart logic
            });
        }
    }

    @Override
    public int getItemCount() {
        if (products.isEmpty()) return 0;
        return Integer.MAX_VALUE; // For infinite scroll
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct, ivCart;
        TextView tvProductName, tvProductPrice;
        View llInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            ivCart = itemView.findViewById(R.id.iv_cart);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            llInfo = itemView.findViewById(R.id.llInfo);
        }
    }
}
