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

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {

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

        // Tìm vị trí thẻ nào được cấu hình chọn mặc định ban đầu (Ví dụ: ZaloPay)
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

        // Đổ dữ liệu hình ảnh và chữ vào các thẻ UI
        holder.ivIcon.setImageResource(item.getIconRes());
        holder.tvName.setText(item.getName());
        holder.tvDetail.setText(item.getDetail());

        // Xử lý logic bật/tắt icon dấu tick dựa theo vị trí đang được click chọn
        boolean isSelected = (position == selectedPosition);
        holder.ivCheck.setImageResource(
                isSelected ? R.drawable.ic_checkbox_checked
                        : R.drawable.ic_checkbox_unchecked);

        // Bắt sự kiện khi người dùng click chọn 1 dòng phương thức
        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            int prev = selectedPosition;
            selectedPosition = currentPos;

            // 1. Tắt dấu tick ở thẻ cũ và ép nó cập nhật lại UI
            if (prev != -1 && prev != selectedPosition) {
                items.get(prev).setSelected(false);
                notifyItemChanged(prev);
            }

            // 2. Bật dấu tick ở thẻ mới chọn và cập nhật UI ngay lập tức
            items.get(selectedPosition).setSelected(true);
            notifyItemChanged(selectedPosition);

            // 3. Truyền dữ liệu phương thức đã chọn ra ngoài BottomSheet
            if (listener != null) {
                listener.onSelected(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

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