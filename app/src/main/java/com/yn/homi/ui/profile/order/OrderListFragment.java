package com.yn.homi.ui.profile.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yn.homi.R;
import com.yn.homi.ui.profile.order.OrderManager;

import java.util.List;

public class OrderListFragment extends Fragment implements OrderManager.OrderChangeListener {

    private static final String ARG_STATUS = "status";
    private RecyclerView recyclerView;
    private View layoutEmpty;
    private String statusStr;

    public static OrderListFragment newInstance(String status) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        // Lấy danh sách theo status
        statusStr = getArguments() != null
                ? getArguments().getString(ARG_STATUS, "ALL") : "ALL";

        loadOrders();
        OrderManager.getInstance(getContext()).addOrderChangeListener(this);

        return view;
    }

    private void loadOrders() {
        List<Order> orders;
        if (statusStr.equals("ALL")) {
            orders = OrderManager.getInstance(getContext()).getOrders();
        } else {
            try {
                Order.Status status = Order.Status.valueOf(statusStr);
                orders = OrderManager.getInstance(getContext()).getOrdersByStatus(status);
            } catch (IllegalArgumentException e) {
                orders = java.util.Collections.emptyList();
            }
        }

        if (orders.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new OrderCardAdapter(getContext(), orders));
        }
    }

    @Override
    public void onOrdersChanged() {
        if (isAdded()) {
            getActivity().runOnUiThread(this::loadOrders);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        OrderManager.getInstance(getContext()).removeOrderChangeListener(this);
    }
}
