package com.yn.homi.ui.profile.order;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.yn.homi.R;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private Context context;
    private List<OrderItem> items;
    private String orderId;
    private Order.Status orderStatus;

    public OrderItemAdapter(Context context, List<OrderItem> items) {
        this.context = context;
        this.items = items;
    }

    public void setOrderInfo(String orderId, Order.Status orderStatus) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = items.get(position);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText("USD " + (int) item.getPrice());

        // Gộp Color và Quantity vào một TextView duy nhất
        holder.tvColorQty.setText(item.getColor() + " · Qty: " + item.getQuantity());

        // Trạng thái của từng món hàng (nếu có hiển thị)
        if (holder.tvStatus != null) {
            holder.tvStatus.setText(item.getPackageStatus());
        }

        Glide.with(context)
                .load(item.getImageUrl())
                .into(holder.imgProduct);

        if (orderStatus == Order.Status.COMPLETED) {
            holder.btnWriteReview.setVisibility(View.VISIBLE);
            holder.btnWriteReview.setOnClickListener(v -> {
                Intent intent = new Intent(context, WriteReviewActivity.class);
                intent.putExtra("PRODUCT_ID", item.getProductId());
                intent.putExtra("PRODUCT_NAME", item.getName());
                intent.putExtra("ORDER_ID", orderId);
                context.startActivity(intent);
            });
        } else {
            holder.btnWriteReview.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvColorQty, tvStatus;
        MaterialButton btnWriteReview;

        ViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvColorQty = itemView.findViewById(R.id.tvColorQty);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnWriteReview = itemView.findViewById(R.id.btnWriteReview);
        }
    }
}
