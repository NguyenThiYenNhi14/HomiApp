package com.yn.homi.ui.profile.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;
import com.google.android.material.button.MaterialButton;
import com.yn.homi.ui.profile.MockDataProvider;

import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Get Order ID from intent
        String orderId = getIntent().getStringExtra("ORDER_ID");

        // Load order data
        Order order = findOrderById(orderId);
        if (order == null) finish();
        else bindOrderData(order);
    }

    private Order findOrderById(String orderId) {
        if (orderId == null) return null;
        for (Order o : MockDataProvider.getAllOrders(this)) {
            if (o.getOrderId().equals(orderId)) return o;
        }
        return null;
    }

    private void bindOrderData(Order order) {
        // Toolbar title
        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        tvTitle.setText("Order #" + order.getOrderId());

        // Status card
        TextView tvOrderId = findViewById(R.id.tvOrderIdDetail);
        tvOrderId.setText("Order #" + order.getOrderId());

        TextView tvStatus = findViewById(R.id.tvStatusDetail);
        tvStatus.setText(formatStatus(order.getStatus()));

        TextView tvDate = findViewById(R.id.tvOrderDateDetail);
        tvDate.setText("Placed on " + order.getPlacedAt());

        // Items RecyclerView
        RecyclerView rvItems = findViewById(R.id.rvDetailItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(new OrderItemAdapter(this, order.getItems()));
        rvItems.setNestedScrollingEnabled(false);

        // Shipping address
        TextView tvAddress = findViewById(R.id.tvShippingAddress);
        tvAddress.setText(order.getShippingAddress());

        // Payment summary
        TextView tvSubtotal = findViewById(R.id.tvSubtotal);
        tvSubtotal.setText(String.format(Locale.US, "$%.2f", order.getSubtotal()));

        TextView tvShipping = findViewById(R.id.tvShippingFee);
        tvShipping.setText(order.getShippingFee() == 0 ? "Free" :
                String.format(Locale.US, "$%.2f", order.getShippingFee()));

        TextView tvTotal = findViewById(R.id.tvTotalDetail);
        tvTotal.setText(String.format(Locale.US, "$%.2f", order.getTotal()));

        // Tracking card — show for SHIPPED or PARTIALLY_SHIPPED
        View cardTracking = findViewById(R.id.cardTracking);
        boolean isShipping = order.getStatus() == Order.Status.SHIPPED || order.getStatus() == Order.Status.PARTIALLY_SHIPPED;
        if (isShipping && order.getTrackingCode() != null && !order.getTrackingCode().isEmpty()) {
            cardTracking.setVisibility(View.VISIBLE);
            TextView tvTracking = findViewById(R.id.tvTrackingCode);
            tvTracking.setText(order.getTrackingCode());
        } else {
            cardTracking.setVisibility(View.GONE);
        }

        // Bottom action button
        MaterialButton btnAction = findViewById(R.id.btnDetailAction);
        configureActionButton(btnAction, order);
    }

    private void configureActionButton(MaterialButton btn, Order order) {
        switch (order.getStatus()) {
            case PARTIALLY_SHIPPED:
            case SHIPPED:
                btn.setText("Track Package");
                btn.setOnClickListener(v -> {
                    Intent intent = new Intent(this, TrackPackageActivity.class);
                    intent.putExtra("ORDER_ID", order.getOrderId());
                    startActivity(intent);
                });
                break;
            case PENDING:
            case PROCESSING:
                btn.setText("Messages");
                btn.setOnClickListener(v -> { /* open chat */ });
                break;
            case RETURNED:
            case CANCELLED:
                btn.setText("Contact Support");
                btn.setOnClickListener(v -> { /* open support */ });
                break;
            default:
                btn.setVisibility(View.GONE);
                break;
        }
    }

    private String formatStatus(Order.Status status) {
        switch (status) {
            case PENDING: return "Pending";
            case PROCESSING: return "Processing";
            case PARTIALLY_SHIPPED: return "Partially Shipped";
            case SHIPPED: return "Shipped";
            case RETURNED: return "Returned";
            case CANCELLED: return "Cancelled";
            default: return status.name();
        }
    }
}
