package com.yn.homi.model;

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
import com.yn.homi.model.OrderItem;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private Context context;
    private List<OrderItem> items;

    public OrderItemAdapter(Context context, List<OrderItem> items) {
        this.context = context;
        this.items = items;
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
                .load(item.getImageResId())
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvColorQty, tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvColorQty = itemView.findViewById(R.id.tvColorQty);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}