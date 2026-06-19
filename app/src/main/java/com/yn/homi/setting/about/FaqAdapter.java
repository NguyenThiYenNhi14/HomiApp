package com.yn.homi.setting.about;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;

import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.ViewHolder> {

    private List<FaqItem> faqList;

    public FaqAdapter(List<FaqItem> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FaqItem item = faqList.get(position);

        // Gán nội dung
        holder.tvQuestion.setText(item.getQuestion());
        holder.tvAnswer.setText(item.getAnswer());

        // Kiểm tra trạng thái mở/đóng -> hiển thị đúng
        if (item.isExpanded()) {
            holder.tvAnswer.setVisibility(View.VISIBLE);
            holder.ivArrow.setRotation(180f); // mũi tên xoay lên
        } else {
            holder.tvAnswer.setVisibility(View.GONE);
            holder.ivArrow.setRotation(0f);   // mũi tên xuống
        }

        // Xử lý khi user bấm vào câu hỏi
        holder.layoutQuestion.setOnClickListener(v -> {
            boolean currentState = item.isExpanded();
            item.setExpanded(!currentState); // toggle true/false
            notifyItemChanged(position);     // cập nhật lại item này
        });
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer;
        ImageView ivArrow;
        LinearLayout layoutQuestion;

        ViewHolder(View v) {
            super(v);
            tvQuestion    = v.findViewById(R.id.tvQuestion);
            tvAnswer      = v.findViewById(R.id.tvAnswer);
            ivArrow       = v.findViewById(R.id.ivArrow);
            layoutQuestion = v.findViewById(R.id.layoutQuestion);
        }
    }
}