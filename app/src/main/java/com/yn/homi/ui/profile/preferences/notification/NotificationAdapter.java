package com.yn.homi.ui.profile.preferences.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;

import java.util.List;
public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationItem> items;
    private Context context;

    public NotificationAdapter(Context ctx, List<NotificationItem> items) {
        this.context = ctx;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NotificationItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDesc.setText(item.getDescription());
        holder.tvTime.setText(item.getTimeAgo());
        holder.img.setImageResource(item.getImage());
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvTitle, tvDesc, tvTime;

        ViewHolder(View v) {
            super(v);
            img     = v.findViewById(R.id.imgNotif);
            tvTitle = v.findViewById(R.id.tvNotifTitle);
            tvDesc  = v.findViewById(R.id.tvNotifDesc);
            tvTime  = v.findViewById(R.id.tvTime);
        }
    }
}
