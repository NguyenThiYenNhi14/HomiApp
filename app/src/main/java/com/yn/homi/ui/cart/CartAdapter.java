package com.yn.homi.ui.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yn.homi.R;
import com.yn.homi.ui.cart.CartManager;
import com.yn.homi.data.model.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartItemInteractionListener {
        void onItemQuantityChanged(CartItem item, int newQuantity);
        void onItemVariantClicked(CartItem item);
        void onItemClicked(CartItem item);
        void onItemSelectionChanged(CartItem item, boolean isSelected);
    }

    private final Context context;
    private final List<CartItem> cartItems;
    private final OnCartItemInteractionListener listener;
    private boolean isEditable = true;

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemInteractionListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void setEditable(boolean editable) {
        this.isEditable = editable;
    }

    public void updateData(List<CartItem> newItems) {
        this.cartItems.clear();
        this.cartItems.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(item.isSelected());
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            if (listener != null) listener.onItemSelectionChanged(item, isChecked);
        });

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.img_chair)
                .error(R.drawable.img_chair)
                .into(holder.imgProduct);

        holder.tvProductName.setText(item.getName());

        // Cho phép click vào ảnh để xem chi tiết
        holder.imgProduct.setOnClickListener(v -> {
            if (listener != null) listener.onItemClicked(item);
        });

        // Hiển thị biến thể (Chỉ hiển thị Màu sắc theo yêu cầu)
        String color = item.getSelectedColor();
        if (color != null && !color.isEmpty()) {
            holder.tvProductVariant.setVisibility(View.VISIBLE);
            holder.tvProductVariant.setText(color);
        } else {
            holder.tvProductVariant.setVisibility(View.GONE);
        }

        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        
        if (isEditable) {
            holder.layoutStepper.setVisibility(View.VISIBLE);
            holder.tvQuantityFixed.setVisibility(View.GONE);
            
            // Cho phép click vào variant để đổi
            holder.tvProductVariant.setOnClickListener(v -> {
                if (listener != null) listener.onItemVariantClicked(item);
            });
        } else {
            holder.layoutStepper.setVisibility(View.GONE);
            holder.tvQuantityFixed.setVisibility(View.VISIBLE);
            holder.tvQuantityFixed.setText("x" + item.getQuantity());
            holder.tvProductVariant.setOnClickListener(null);
            holder.tvProductVariant.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        // Hiển thị GIÁ món hàng
        holder.tvProductPrice.setText(getUSDString(item.getPrice()));

        holder.btnPlus.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            if (listener != null) {
                listener.onItemQuantityChanged(item, newQty);
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQty = item.getQuantity() - 1;
                if (listener != null) {
                    listener.onItemQuantityChanged(item, newQty);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems == null ? 0 : cartItems.size();
    }

    private String getUSDString(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice, tvQuantity, tvQuantityFixed, tvProductVariant;
        TextView btnMinus, btnPlus;
        View layoutStepper;
        android.widget.CheckBox cbSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect        = itemView.findViewById(R.id.cbSelect);
            imgProduct      = itemView.findViewById(R.id.imgProduct);
            tvProductName   = itemView.findViewById(R.id.tvProductName);
            tvProductVariant = itemView.findViewById(R.id.tvProductVariant);
            tvProductPrice  = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity      = itemView.findViewById(R.id.tvQuantity);
            tvQuantityFixed = itemView.findViewById(R.id.tvQuantityFixed);
            btnMinus        = itemView.findViewById(R.id.btnMinus);
            btnPlus         = itemView.findViewById(R.id.btnPlus);
            layoutStepper   = itemView.findViewById(R.id.layoutStepper);
        }
    }
}
