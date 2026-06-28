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
import com.yn.homi.ui.profile.MockDataProvider;

import java.util.List;

public class OrderListFragment extends Fragment {

    private static final String ARG_STATUS = "status";

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

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        View layoutEmpty = view.findViewById(R.id.layoutEmpty);

        // Lấy danh sách theo status
        String statusStr = getArguments() != null
                ? getArguments().getString(ARG_STATUS, "ALL") : "ALL";

        List<Order> orders;
        if (statusStr.equals("ALL")) {
            orders = MockDataProvider.getAllOrders(getContext());
        } else {
            try {
                Order.Status status = Order.Status.valueOf(statusStr);
                orders = MockDataProvider.getOrdersByStatus(getContext(), status);
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

        return view;
    }
}
