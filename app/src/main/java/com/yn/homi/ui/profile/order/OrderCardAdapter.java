package com.yn.homi.ui.profile.order;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.yn.homi.R;

import java.util.List;

public class OrderCardAdapter extends RecyclerView.Adapter<OrderCardAdapter.ViewHolder> {

    private Context context;
    private List<Order> orders;

    public OrderCardAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_order_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);

        // Set Order ID
        holder.tvOrderId.setText("Order ID #" + order.getOrderId());

        // 1. Ngày đặt + số lượng items
        int itemCount = order.getItems().size();
        holder.tvOrderDate.setText("Placed " + order.getPlacedAt() + " · " + itemCount + " item" + (itemCount > 1 ? "s" : ""));

        // 2. Status badge
        applyStatusBadge(holder.tvStatusBadge, order.getStatus());

        // 3. Hiện tối đa 2 sản phẩm (Logic rút gọn danh sách)
        List<OrderItem> visibleItems = order.getItems().size() > 2
                ? order.getItems().subList(0, 2)
                : order.getItems();

        OrderItemAdapter itemAdapter = new OrderItemAdapter(context, visibleItems);
        holder.rvItems.setLayoutManager(new LinearLayoutManager(context));
        holder.rvItems.setAdapter(itemAdapter);
        holder.rvItems.setNestedScrollingEnabled(false);

        // 4. Xử lý hiển thị text "More items"
        int extra = order.getItems().size() - 2;
        if (extra > 0) {
            holder.tvMoreItems.setVisibility(View.VISIBLE);
            holder.tvMoreItems.setText("+" + extra + " more item" + (extra > 1 ? "s" : ""));
        } else {
            holder.tvMoreItems.setVisibility(View.GONE);
        }

        // 5. Hiển thị tổng tiền
        holder.tvTotal.setText(String.format("$%.2f", order.getTotal()));

        // Order Details button
        holder.btnOrderDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("ORDER_ID", order.getOrderId());
            context.startActivity(intent);
        });

        // Primary action button — thay đổi theo status
        configurePrimaryButton(holder.btnPrimaryAction, order);
    }

    private void applyStatusBadge(TextView badge, Order.Status status) {
        String label;
        int bgColor, textColor;
        switch (status) {
            case PENDING:
                label = "Pending";
                bgColor = 0xFFF2F2F2; textColor = 0xFF555555; break;
            case PROCESSING:
                label = "Processing";
                bgColor = 0xFFF2F2F2; textColor = 0xFF555555; break;
            case PARTIALLY_SHIPPED:
                label = "Partially Shipped";
                bgColor = 0xFFF2F2F2; textColor = 0xFF555555; break;
            case SHIPPED:
                label = "Shipped";
                bgColor = 0xFF111111; textColor = 0xFFFFFFFF; break;
            case COMPLETED:
                label = "Completed";
                bgColor = 0xFF4CAF50; textColor = 0xFFFFFFFF; break;
            case RETURNED:
                label = "Returned";
                bgColor = 0xFFEEEEEE; textColor = 0xFF888888; break;
            case CANCELLED:
                label = "Cancelled";
                bgColor = 0xFFEEEEEE; textColor = 0xFF888888; break;
            default:
                label = status.name();
                bgColor = 0xFFF2F2F2; textColor = 0xFF888888;
        }
        badge.setText(label);
        badge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bgColor));
        badge.setTextColor(textColor);
    }

    private void configurePrimaryButton(MaterialButton btn, Order order) {
        int black = android.graphics.Color.parseColor("#111111");
        int gray  = android.graphics.Color.parseColor("#AAAAAA");
        switch (order.getStatus()) {
            case PENDING:
            case PROCESSING:
                btn.setText("Messages");
                btn.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(black));
                btn.setOnClickListener(v -> { /* mở chat */ });
                break;

            case PARTIALLY_SHIPPED:
            case SHIPPED:
                btn.setText("Track Package");
                btn.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(black));
                btn.setOnClickListener(v -> {
                    Intent intent = new Intent(context, TrackPackageActivity.class);
                    intent.putExtra("ORDER_ID", order.getOrderId());
                    context.startActivity(intent);
                });
                break;

            case COMPLETED:
                btn.setText("Review");
                btn.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(black));
                btn.setOnClickListener(v -> { /* Open Review logic */ });
                break;

            case RETURNED:
            case CANCELLED:
                btn.setText("Contact Support");
                btn.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(gray));
                btn.setOnClickListener(v -> { /* mở support */ });
                break;
            default:
                btn.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public int getItemCount() { return orders.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvStatusBadge, tvTotal, tvMoreItems;
        RecyclerView rvItems;
        MaterialButton btnOrderDetails, btnPrimaryAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderId       = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate     = itemView.findViewById(R.id.tvOrderDate);
            tvStatusBadge   = itemView.findViewById(R.id.tvStatusBadge);
            tvTotal         = itemView.findViewById(R.id.tvTotal);
            tvMoreItems     = itemView.findViewById(R.id.tvMoreItems);
            rvItems         = itemView.findViewById(R.id.rvOrderItems);
            btnOrderDetails = itemView.findViewById(R.id.btnOrderDetails);
            btnPrimaryAction = itemView.findViewById(R.id.btnPrimaryAction);
        }
    }
}
