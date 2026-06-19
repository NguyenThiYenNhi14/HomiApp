package com.yn.homi.setting.wishlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<WishlistItem> items;
    private Context context;

    public WishlistAdapter(Context context, List<WishlistItem> items) {
        this.context = context;
        this.items = items;
    }

    // Inflate (create) the item card view
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_wishlist, parent, false);
        return new ViewHolder(v);
    }

    // Fill each card with data
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WishlistItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvColor.setText("Color: " + item.getColor());
        holder.tvPrice.setText(item.getPrice());
        holder.imgProduct.setImageResource(item.getImage());

        // Delete button removes item from list
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) { items.remove(pos); notifyItemRemoved(pos); }
            notifyItemRangeChanged(position, items.size());
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    // ViewHolder finds views inside each row card
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnDelete;
        TextView tvName, tvColor, tvPrice;

        ViewHolder(View v) {
            super(v);
            imgProduct = v.findViewById(R.id.imgProduct);
            btnDelete  = v.findViewById(R.id.btnDelete);
            tvName     = v.findViewById(R.id.tvProductName);
            tvColor    = v.findViewById(R.id.tvColor);
            tvPrice    = v.findViewById(R.id.tvPrice);
        }
    }
}