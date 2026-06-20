package com.yn.homi.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import android.util.Log;
import com.yn.homi.models.Product;
import com.yn.homi.ProductDetailActivity;
import com.yn.homi.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private boolean useHorizontalStyle = false;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public ProductAdapter(List<Product> productList, boolean useHorizontalStyle) {
        this.productList = productList;
        this.useHorizontalStyle = useHorizontalStyle;
    }

    public void updateData(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        if (useHorizontalStyle) {
            view.getLayoutParams().width = (int) (parent.getResources().getDisplayMetrics().widthPixels * 0.6);
        }
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        final Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        
        // Hiển thị banner sale trên ảnh nếu có giảm giá hoặc có tag sale
        boolean hasSaleEffect = product.getDiscountPercent() > 0 || product.isOnSale() || 
                (product.getTags() != null && (product.getTags().contains("flash_sale") || product.getTags().contains("sale")));
        
        if (hasSaleEffect) {
            holder.llSaleBanner.setVisibility(View.VISIBLE);
            holder.tvSalePriceOnImage.setText(String.format("$ %.2f", product.getPrice()));
        } else {
            holder.llSaleBanner.setVisibility(View.GONE);
        }

        // 2. Hiển thị giá phía dưới
        if (product.getOriginalPrice() > product.getPrice() || product.getDiscountPercent() > 0) {
            double displayOriginalPrice = product.getOriginalPrice() > 0 ? product.getOriginalPrice() : product.getPrice() * 1.2;
            String salePrice = String.format("$%.2f", product.getPrice());
            String originalPriceStr = String.format("$%.2f", displayOriginalPrice);
            String combined = salePrice + "  " + originalPriceStr;
            
            SpannableStringBuilder ssb = new SpannableStringBuilder(combined);
            int start = salePrice.length() + 2;
            int end = combined.length();
            
            // Giá gốc bị gạch ngang màu gray bên cạnh
            ssb.setSpan(new ForegroundColorSpan(Color.GRAY), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new AbsoluteSizeSpan(12, true), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            
            holder.tvPrice.setText(ssb);
        } else {
            holder.tvPrice.setText(String.format("$%.2f", product.getPrice()));
        }

        // 3. Hiển thị rating kèm reviewCount: "★ 4.8  (215)"
        holder.tvRating.setText(String.format("★ %.1f  (%d)", product.getRating(), product.getReviewCount()));

        // 1. Dùng ảnh chất lượng cao nếu có
        String imageUrl = product.getThumbnailUrl();
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            imageUrl = product.getImageUrls().get(0);
        }
        final String finalImageUrl = imageUrl;

        Glide.with(holder.itemView.getContext())
                .load(finalImageUrl)
                .placeholder(android.R.color.white)
                .error(android.R.color.white)
                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        Log.e("GlideError", "Failed to load image for product: " + product.getName() + " URL: " + finalImageUrl, e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.ivProduct);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            holder.itemView.getContext().startActivity(intent);
        });

        if (holder.ivAddToCart != null) {
            holder.ivAddToCart.setOnClickListener(v -> {
                // TODO: Implement add to cart logic
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct, ivAddToCart;
        TextView tvName, tvPrice, tvRating;
        View llSaleBanner;
        TextView tvSalePriceOnImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            ivAddToCart = itemView.findViewById(R.id.iv_add_to_cart);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvRating = itemView.findViewById(R.id.tv_product_rating);
            llSaleBanner = itemView.findViewById(R.id.ll_sale_banner);
            tvSalePriceOnImage = itemView.findViewById(R.id.tv_sale_price_on_image);
        }
    }
}
