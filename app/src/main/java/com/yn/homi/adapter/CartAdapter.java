package com.yn.homi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yn.homi.R;
import com.yn.homi.model.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface CartActionListener {
        void onQuantityChanged(CartItem item, int newQty);
        void onRemove(CartItem item);
    }

    private final Context            context;
    private final List<CartItem>     items;
    private final CartActionListener listener;

    public CartAdapter(Context context, List<CartItem> items, CartActionListener listener) {
        this.context  = context;
        this.items    = items;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CartItem item = items.get(position);

        h.tvName.setText(item.getName());
        h.tvPrice.setText(String.format("$%.2f", item.getPrice()));
        h.tvQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.img_chair)
                .into(h.imgProduct);

        // Nút trừ
        h.btnMinus.setOnClickListener(v -> {
            int newQty = item.getQuantity() - 1;
            listener.onQuantityChanged(item, newQty);  // CartManager xử lý remove nếu <= 0
        });

        // Nút cộng
        h.btnPlus.setOnClickListener(v -> {
            listener.onQuantityChanged(item, item.getQuantity() + 1);
        });

        // Nút xoá (thùng rác đỏ)
        h.btnDelete.setOnClickListener(v -> listener.onRemove(item));
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView   imgProduct;
        TextView    tvName, tvPrice, tvQuantity;
        ImageButton btnMinus, btnPlus, btnDelete;

        ViewHolder(@NonNull View v) {
            super(v);
            imgProduct  = v.findViewById(R.id.imgProduct);
            tvName      = v.findViewById(R.id.tvProductName);
            tvPrice     = v.findViewById(R.id.tvProductPrice);
            tvQuantity  = v.findViewById(R.id.tvQuantity);
            btnMinus    = v.findViewById(R.id.btnMinus);
            btnPlus     = v.findViewById(R.id.btnPlus);
        }
    }
}