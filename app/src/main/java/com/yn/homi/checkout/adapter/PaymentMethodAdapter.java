package com.yn.homi.checkout.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;
import com.yn.homi.checkout.model.PaymentMethod;

import java.util.List;

public class PaymentMethodAdapter
        extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {

    public interface OnPaymentSelectedListener {
        void onSelected(PaymentMethod method);
    }

    private final Context context;
    private final List<PaymentMethod> items;
    private final OnPaymentSelectedListener listener;
    private int selectedPosition = -1;

    public PaymentMethodAdapter(Context context,
                                List<PaymentMethod> items,
                                OnPaymentSelectedListener listener) {
        this.context  = context;
        this.items    = items;
        this.listener = listener;

        // Find initially-selected item
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isSelected()) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_payment_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentMethod item = items.get(position);

        holder.ivIcon.setImageResource(item.getIconRes());
        holder.tvName.setText(item.getName());
        holder.tvDetail.setText(item.getDetail());

        // Show checked / unchecked icon
        boolean isSelected = (position == selectedPosition);
        holder.ivCheck.setImageResource(
                isSelected ? R.drawable.ic_checkbox_checked
                        : R.drawable.ic_checkbox_unchecked);

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Deselect old, select new
            if (prev != -1) {
                items.get(prev).setSelected(false);
                notifyItemChanged(prev);
            }
            items.get(selectedPosition).setSelected(true);
            notifyItemChanged(selectedPosition);

            if (listener != null) listener.onSelected(item);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivCheck;
        TextView  tvName, tvDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon   = itemView.findViewById(R.id.ivPaymentIcon);
            ivCheck  = itemView.findViewById(R.id.ivCheck);
            tvName   = itemView.findViewById(R.id.tvPaymentName);
            tvDetail = itemView.findViewById(R.id.tvPaymentDetail);
        }
    }
}