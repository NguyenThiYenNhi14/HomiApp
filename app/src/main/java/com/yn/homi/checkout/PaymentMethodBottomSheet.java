package com.yn.homi.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;
import com.yn.homi.checkout.adapter.PaymentMethodAdapter;
import com.yn.homi.checkout.model.PaymentMethod;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodBottomSheet extends BottomSheetDialogFragment {

    // Callback trả kết quả về CheckoutActivity
    public interface OnPaymentConfirmedListener {
        void onConfirmed(PaymentMethod method);
    }

    private OnPaymentConfirmedListener confirmListener;
    private PaymentMethod selectedMethod = null;

    public void setOnPaymentConfirmedListener(OnPaymentConfirmedListener listener) {
        this.confirmListener = listener;
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

        // --- Dữ liệu mẫu (sau này lấy từ ViewModel / API) ---
        List<PaymentMethod> methods = new ArrayList<>();
        methods.add(new PaymentMethod(
                R.drawable.ic_paypal,
                "PayPal",
                "alex*****@mail.com",
                false));
        methods.add(new PaymentMethod(
                R.drawable.ic_mastercard,
                "Mastercard",
                "5284 8922 7424 ****",
                true));                       // default selected

        // Ghi nhớ lựa chọn ban đầu
        selectedMethod = methods.get(1);

        PaymentMethodAdapter adapter = new PaymentMethodAdapter(
                requireContext(),
                methods,
                method -> selectedMethod = method   // cập nhật khi user chọn
        );

        rvPayments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPayments.setAdapter(adapter);

        // Checkout button
        btnCheckout.setOnClickListener(v -> {
            if (selectedMethod != null && confirmListener != null) {
                confirmListener.onConfirmed(selectedMethod);
            }
            dismiss();
        });

        // Add payment method
        view.findViewById(R.id.layoutAddPayment).setOnClickListener(v -> {
            // TODO: mở màn hình thêm thẻ mới
        });
    }
}