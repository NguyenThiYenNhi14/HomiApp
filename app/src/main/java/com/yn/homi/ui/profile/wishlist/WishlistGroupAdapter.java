package com.yn.homi.ui.profile.wishlist;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import com.yn.homi.R;
import com.yn.homi.data.model.Wishlist;
import com.yn.homi.utils.FavoritesManager;
import java.util.List;

public class WishlistGroupAdapter extends RecyclerView.Adapter<WishlistGroupAdapter.ViewHolder> {

    private List<Wishlist> wishlists;
    private Context context;
    private FavoritesManager favoritesManager;

    public WishlistGroupAdapter(Context context, List<Wishlist> wishlists) {
        this.context = context;
        this.wishlists = wishlists;
        this.favoritesManager = new FavoritesManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wishlist wishlist = wishlists.get(position);
        holder.tvListName.setText(wishlist.getName());
        int count = wishlist.getItems() != null ? wishlist.getItems().size() : 0;
        
        String countText;
        if (count == 1) {
            countText = context.getString(R.string.item_count, count);
        } else {
            countText = context.getString(R.string.items_count, count);
        }
        holder.tvItemCount.setText(countText);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WishlistDetailActivity.class);
            intent.putExtra("WISHLIST_NAME", wishlist.getName());
            context.startActivity(intent);
        });

        holder.btnMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            popup.getMenu().add(0, 1, 0, context.getString(R.string.rename));
            popup.getMenu().add(0, 2, 1, context.getString(R.string.delete));

            popup.setOnMenuItemClickListener(item -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return false;
                
                if (item.getItemId() == 1) {
                    showRenameDialog(wishlists.get(currentPos), currentPos);
                } else if (item.getItemId() == 2) {
                    showDeleteConfirmDialog(wishlists.get(currentPos), currentPos);
                }
                return true;
            });
            popup.show();
        });
    }

    private void showRenameDialog(Wishlist wishlist, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.rename_wishlist);

        final EditText input = new EditText(context);
        input.setText(wishlist.getName());
        input.setSelectAllOnFocus(true);
        
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = padding;
        params.rightMargin = padding;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(wishlist.getName())) {
                favoritesManager.renameWishlist(wishlist.getName(), newName);
                wishlist.setName(newName);
                notifyItemChanged(position);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void showDeleteConfirmDialog(Wishlist wishlist, int position) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.delete_wishlist)
                .setMessage(context.getString(R.string.delete_wishlist_confirm, wishlist.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    deleteItem(position);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void deleteItem(int position) {
        if (position >= 0 && position < wishlists.size()) {
            Wishlist wishlist = wishlists.get(position);
            favoritesManager.deleteWishlist(wishlist.getName());
            wishlists.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, wishlists.size());
            
            if (context instanceof WishlistActivity) {
                ((WishlistActivity) context).checkEmptyState();
            }
        }
    }

    @Override
    public int getItemCount() {
        return wishlists.size();
    }

    public void updateData(List<Wishlist> newList) {
        this.wishlists = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvListName, tvItemCount;
        View btnMoreOptions;

        ViewHolder(View itemView) {
            super(itemView);
            tvListName = itemView.findViewById(R.id.tv_list_name);
            tvItemCount = itemView.findViewById(R.id.tv_item_count);
            btnMoreOptions = itemView.findViewById(R.id.btn_more_options);
        }
    }
}
