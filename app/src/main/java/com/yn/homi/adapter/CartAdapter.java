package com.yn.homi.adapter;

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
import com.yn.homi.cart.CartManager;
import com.yn.homi.model.CartItem;

import java.text.NumberFormat; // --- BỔ SUNG ĐỂ ĐỊNH DẠNG VNĐ ---
import java.util.List;
import java.util.Locale;      // --- BỔ SUNG ĐỂ ĐỊNH DẠNG VNĐ ---

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartItemInteractionListener {
        void onItemQuantityChanged(String itemId, int newQuantity);
        // void onItemRemove(CartItem item); // Gỡ bỏ listener xóa tĩnh vì dùng Swipe
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.img_chair)
                .error(R.drawable.img_chair)
                .into(holder.imgProduct);

        holder.tvProductName.setText(item.getName());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        
        if (isEditable) {
            holder.layoutStepper.setVisibility(View.VISIBLE);
            holder.tvQuantityFixed.setVisibility(View.GONE);
        } else {
            holder.layoutStepper.setVisibility(View.GONE);
            holder.tvQuantityFixed.setVisibility(View.VISIBLE);
            holder.tvQuantityFixed.setText("x" + item.getQuantity());
        }

        // --- SỬA CHỔ NÀY: Hiển thị TỔNG TIỀN món hàng, định dạng VNĐ ---
        holder.tvProductPrice.setText(getVNDString(item.getItemTotal()));

        // Tăng số lượng (+)
        holder.btnPlus.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            item.setQuantity(newQty); // Cập nhật tạm thời để UI stepper mượt
            holder.tvQuantity.setText(String.valueOf(newQty));
            if (listener != null) {
                listener.onItemQuantityChanged(item.getId(), newQty);
            }
        });

        // Giảm số lượng (−)
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQty = item.getQuantity() - 1;
                item.setQuantity(newQty); // Cập nhật tạm thời
                holder.tvQuantity.setText(String.valueOf(newQty));
                if (listener != null) {
                    listener.onItemQuantityChanged(item.getId(), newQty);
                }
            }
        });

        // --- GỠ BỎ HANDLE CLICK XÓA CỨNG btnDelete TẠI ĐÂY ---
        /*
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemRemove(item);
            }
        });
        */
    }

    @Override
    public int getItemCount() {
        return cartItems == null ? 0 : cartItems.size();
    }

    // --- BỔ SUNG HÀM ĐỊNH DẠNG VNĐ ---
    private String getVNDString(double amount) {
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyVN = NumberFormat.getCurrencyInstance(localeVN);
        String formatted = currencyVN.format(amount);
        // Tùy chỉnh: Đảm bảo có chữ VNĐ phía sau hoặc định dạng chuẩn của Locale
        return formatted.replace("₫", "").trim() + " VND";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice, tvQuantity, tvQuantityFixed;
        TextView btnMinus, btnPlus;
        View layoutStepper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct      = itemView.findViewById(R.id.imgProduct);
            tvProductName   = itemView.findViewById(R.id.tvProductName);
            tvProductPrice  = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity      = itemView.findViewById(R.id.tvQuantity);
            tvQuantityFixed = itemView.findViewById(R.id.tvQuantityFixed);
            btnMinus        = itemView.findViewById(R.id.btnMinus);
            btnPlus         = itemView.findViewById(R.id.btnPlus);
            layoutStepper   = itemView.findViewById(R.id.layoutStepper);
        }
    }
}