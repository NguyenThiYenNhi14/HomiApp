package com.yn.homi.ui.checkout.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;
import com.yn.homi.ui.checkout.model.PaymentMethod;

import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {

    private final List<PaymentMethod> methods;
    private final OnPaymentMethodSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnPaymentMethodSelectedListener {
        void onSelected(PaymentMethod method);
    }

    public PaymentMethodAdapter(Context context, List<PaymentMethod> methods, OnPaymentMethodSelectedListener listener) {
        this.methods = methods;
        this.listener = listener;
        for (int i = 0; i < methods.size(); i++) {
            if (methods.get(i).isSelected()) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_method, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentMethod method = methods.get(position);
        holder.ivIcon.setImageResource(method.getIconRes());
        holder.tvName.setText(method.getName());
        holder.tvDesc.setText(method.getDescription());
        holder.ivCheck.setImageResource(position == selectedPosition ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onSelected(method);
        });
    }

    @Override
    public int getItemCount() {
        return methods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvDesc;
        ImageView ivCheck;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivPaymentIcon);
            tvName = itemView.findViewById(R.id.tvPaymentName);
            tvDesc = itemView.findViewById(R.id.tvPaymentDetail);
            ivCheck = itemView.findViewById(R.id.ivCheck);
        }
    }
}
