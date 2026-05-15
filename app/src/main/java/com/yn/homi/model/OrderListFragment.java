package com.yn.homi.model;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yn.homi.R;
import com.yn.homi.model.OrderCardAdapter;
import com.yn.homi.model.MockDataProvider;
import com.yn.homi.model.Order;
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
        TextView tvEmpty = view.findViewById(R.id.tvEmpty);

        // Lấy danh sách theo status
        String statusStr = getArguments() != null
                ? getArguments().getString(ARG_STATUS, "ALL") : "ALL";

        List<Order> orders;
        if (statusStr.equals("ALL")) {
            orders = MockDataProvider.getAllOrders();
        } else {
            Order.Status status = Order.Status.valueOf(statusStr);
            orders = MockDataProvider.getOrdersByStatus(status);
        }

        if (orders.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new OrderCardAdapter(getContext(), orders));
        }

        return view;
    }
}