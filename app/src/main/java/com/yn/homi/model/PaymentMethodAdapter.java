package com.yn.homi.model;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;

import java.util.List;

public class PaymentMethodAdapter extends
        RecyclerView.Adapter<PaymentMethodAdapter.CardViewHolder> {

    private List<PaymentCard> cardList;
    private int selectedPosition = 0;
    private OnCardActionListener listener;

    public interface OnCardActionListener {
        void onDeleteCard(int position);
        void onSelectCard(int position);
    }

    public PaymentMethodAdapter(List<PaymentCard> list, OnCardActionListener l) {
        this.cardList = list;
        this.listener = l;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_card, parent, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder h, int position) {
        PaymentCard card = cardList.get(position);

        h.tvCardType.setText(card.getDisplayName());
        h.tvCardNumber.setText(card.getAccountNumber());
        h.tvCardExpiry.setText(card.getSubInfo());
        h.rbSelect.setChecked(position == selectedPosition);
        h.tvDefault.setVisibility(card.isDefault() ? View.VISIBLE : View.GONE);

        // Icon + màu nền theo loại
        switch (card.getType()) {
            case MOMO:
                h.ivCardIcon.setImageResource(R.drawable.momo_logo);
                h.iconBox.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#FFE0F0")));
                break;
            case ZALOPAY:
                h.ivCardIcon.setImageResource(R.drawable.zalopay);
                h.iconBox.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#E0F0FF")));
                break;
            case VISA:
            case MASTERCARD:
                h.ivCardIcon.setImageResource(R.drawable.visa_logo);
                h.iconBox.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
                break;
            case BANK:
                h.ivCardIcon.setImageResource(R.drawable.bank_logo);
                h.iconBox.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
                break;
        }

        // Click chọn thẻ
        h.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = h.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onSelectCard(selectedPosition);
        });

        h.rbSelect.setOnClickListener(v -> h.itemView.performClick());
    }

    @Override
    public int getItemCount() { return cardList.size(); }

    // Xóa item
    public void removeItem(int position) {
        cardList.remove(position);
        notifyItemRemoved(position);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconBox;
        ImageView ivCardIcon;
        TextView tvCardType, tvCardNumber, tvCardExpiry, tvDefault;
        RadioButton rbSelect;

        public CardViewHolder(@NonNull View v) {
            super(v);
            iconBox      = v.findViewById(R.id.iconBox);
            ivCardIcon   = v.findViewById(R.id.ivCardIcon);
            tvCardType   = v.findViewById(R.id.tvCardType);
            tvCardNumber = v.findViewById(R.id.tvCardNumber);
            tvCardExpiry = v.findViewById(R.id.tvCardExpiry);
            tvDefault    = v.findViewById(R.id.tvDefault);
            rbSelect     = v.findViewById(R.id.rbSelect);
        }
    }
}