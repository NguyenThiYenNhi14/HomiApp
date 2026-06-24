package com.yn.homi.setting.wishlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;
import com.bumptech.glide.Glide;
import com.yn.homi.cart.CartManager;
import com.yn.homi.model.CartItem;
import com.yn.homi.models.Product;
import com.yn.homi.utils.FavoritesManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<Product> items;
    private List<Product> itemsFull;
    private Context context;
    private FavoritesManager favoritesManager;
    private String wishlistName;
    private boolean isSelectionMode = false;
    private Set<Product> selectedItems = new HashSet<>();
    private OnSelectionModeListener selectionModeListener;
    private OnCartUpdateListener cartUpdateListener;
    private OnBuyNowListener buyNowListener;

    public interface OnSelectionModeListener {
        void onSelectionModeChanged(boolean enabled);
    }

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    public interface OnBuyNowListener {
        void onBuyNow(Product product);
    }

    public void setOnSelectionModeListener(OnSelectionModeListener listener) {
        this.selectionModeListener = listener;
    }

    public void setOnCartUpdateListener(OnCartUpdateListener listener) {
        this.cartUpdateListener = listener;
    }

    public void setOnBuyNowListener(OnBuyNowListener listener) {
        this.buyNowListener = listener;
    }

    public WishlistAdapter(Context context, List<Product> items, String wishlistName) {
        this.context = context;
        this.items = items;
        this.itemsFull = new ArrayList<>(items);
        this.wishlistName = wishlistName;
        this.favoritesManager = new FavoritesManager(context);
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
        Product item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvColor.setText("Color: Standard"); // Use default if product has no specific color in wishlist
        holder.tvPrice.setText(String.format(Locale.US, "$%.2f", item.getPrice()));
        
        Glide.with(context)
                .load(item.getThumbnailUrl())
                .into(holder.imgProduct);

        // Selection logic
        holder.cbSelect.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(selectedItems.contains(item));
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedItems.add(item);
            } else {
                selectedItems.remove(item);
                if (selectedItems.isEmpty() && isSelectionMode) {
                    setSelectionMode(false);
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                selectedItems.add(item);
                setSelectionMode(true);
                return true;
            }
            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                holder.cbSelect.setChecked(!holder.cbSelect.isChecked());
            } else {
                // Regular click logic (e.g., open detail)
            }
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            CartItem cartItem = new CartItem(
                    item.getId(),
                    item.getName(),
                    item.getPrice(),
                    1,
                    item.getThumbnailUrl()
            );
            CartManager.getInstance(context).addItem(cartItem);
            
            // Remove from wishlist
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                deleteItem(pos);
            }
            
            if (cartUpdateListener != null) {
                cartUpdateListener.onCartUpdated();
            }
        });

        holder.btnBuyNow.setOnClickListener(v -> {
            CartItem cartItem = new CartItem(
                    item.getId(),
                    item.getName(),
                    item.getPrice(),
                    1,
                    item.getThumbnailUrl()
            );
            CartManager.getInstance(context).addItem(cartItem);
            
            // Remove from wishlist
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                deleteItem(pos);
            }
            
            if (cartUpdateListener != null) {
                cartUpdateListener.onCartUpdated();
            }
            
            if (buyNowListener != null) {
                buyNowListener.onBuyNow(item);
            }
        });

        // Delete button removes item from list and manager
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                deleteItem(pos);
            }
        });
    }

    public void setSelectionMode(boolean enabled) {
        if (this.isSelectionMode != enabled) {
            this.isSelectionMode = enabled;
            if (!enabled) {
                selectedItems.clear();
            }
            notifyDataSetChanged();
            if (selectionModeListener != null) {
                selectionModeListener.onSelectionModeChanged(enabled);
            }
        }
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public List<Product> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    public void deleteItem(int position) {
        if (position >= 0 && position < items.size()) {
            Product productToRemove = items.get(position);
            favoritesManager.removeProductFromWishlist(wishlistName, productToRemove.getId());
            
            // Update itemsFull as well
            itemsFull.remove(productToRemove);
            
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size());
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void filter(String text) {
        items.clear();
        if (text.isEmpty()) {
            items.addAll(itemsFull);
        } else {
            text = text.toLowerCase();
            for (Product item : itemsFull) {
                if (item.getName().toLowerCase().contains(text)) {
                    items.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    // ViewHolder finds views inside each row card
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnDelete;
        TextView tvName, tvColor, tvPrice;
        CheckBox cbSelect;
        View btnBuyNow, btnAddToCart;

        ViewHolder(View v) {
            super(v);
            cbSelect   = v.findViewById(R.id.cbSelect);
            imgProduct = v.findViewById(R.id.imgProduct);
            btnDelete  = v.findViewById(R.id.btnDelete);
            tvName     = v.findViewById(R.id.tvProductName);
            tvColor    = v.findViewById(R.id.tvColor);
            tvPrice    = v.findViewById(R.id.tvPrice);
            btnBuyNow  = v.findViewById(R.id.btnBuyNow);
            btnAddToCart = v.findViewById(R.id.btnAddToCart);
        }
    }
}