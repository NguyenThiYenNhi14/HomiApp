package com.yn.homi.ui.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yn.homi.R;
import com.yn.homi.ui.checkout.adapter.PaymentMethodAdapter;
import com.yn.homi.ui.checkout.model.PaymentMethod;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodBottomSheet extends BottomSheetDialogFragment {

    public interface OnPaymentConfirmedListener {
        void onConfirmed(PaymentMethod method);
    }

    private OnPaymentConfirmedListener confirmListener;
    private PaymentMethod selectedMethod = null;
    private String preSelectedName = "Cash on hand"; // Mặc định

    public void setOnPaymentConfirmedListener(OnPaymentConfirmedListener listener) {
        this.confirmListener = listener;
    }

    // Hàm để Activity truyền tên phương thức đang dùng vào
    public void setSelectedMethodName(String name) {
        if (name != null && !name.isEmpty()) {
            this.preSelectedName = name;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottomsheet_payment_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvPayments = view.findViewById(R.id.rvPaymentMethods);
        Button btnCheckout      = view.findViewById(R.id.btnCheckoutSheet);

        // --- Dữ liệu động dựa trên lựa chọn của người dùng ---
        List<PaymentMethod> methods = new ArrayList<>();

        methods.add(new PaymentMethod(
                R.drawable.ic_cash,
                "Cash on hand",
                "Pay when you receive",
                preSelectedName.equals("Cash on hand")));

        methods.add(new PaymentMethod(
                R.drawable.ic_momo,
                "Momo",
                "Thanh toán qua ví Momo khi nhận hàng",
                preSelectedName.equals("Momo")));

        methods.add(new PaymentMethod(
                R.drawable.ic_zalopay,
                "ZaloPay",
                "Thanh toán qua ví ZaloPay khi nhận hàng",
                preSelectedName.equals("ZaloPay")));

        // Tìm phương thức đang được tick để gán vào selectedMethod ban đầu
        for (PaymentMethod m : methods) {
            if (m.isSelected()) {
                selectedMethod = m;
                break;
            }
        }

        PaymentMethodAdapter adapter = new PaymentMethodAdapter(
                requireContext(),
                methods,
                method -> selectedMethod = method // Cập nhật khi user click chọn
        );

        rvPayments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPayments.setAdapter(adapter);

        // Nút Checkout trả về kết quả chính xác
        btnCheckout.setOnClickListener(v -> {
            if (selectedMethod != null && confirmListener != null) {
                confirmListener.onConfirmed(selectedMethod);
            }
            dismiss();
        });

        view.findViewById(R.id.layoutAddPayment).setOnClickListener(v -> {
            // Xử lý thêm phương thức thanh toán mới
        });
    }
}
