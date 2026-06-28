package com.yn.homi.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.yn.homi.R;
import com.yn.homi.data.model.Wishlist;
import java.util.List;

public class WishlistSelectionAdapter extends RecyclerView.Adapter<WishlistSelectionAdapter.ViewHolder> {

    private List<Wishlist> wishlists;
    private OnWishlistClickListener listener;

    public interface OnWishlistClickListener {
        void onWishlistClick(Wishlist wishlist);
    }

    public WishlistSelectionAdapter(List<Wishlist> wishlists, OnWishlistClickListener listener) {
        this.wishlists = wishlists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wishlist wishlist = wishlists.get(position);
        holder.textView.setText(wishlist.getName());
        holder.itemView.setOnClickListener(v -> listener.onWishlistClick(wishlist));
    }

    @Override
    public int getItemCount() {
        return wishlists.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
