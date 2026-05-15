package com.yn.homi.model;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yn.homi.model.WishlistAdapter;
import com.yn.homi.model.WishlistItem;
import com.yn.homi.R;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView recyclerWishlist;
    private WishlistAdapter adapter;
    private List<WishlistItem> wishlistItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        recyclerWishlist = findViewById(R.id.recyclerWishlist);
        ImageView btnBell = findViewById(R.id.btnNotification);

        // Setup RecyclerView — vertical list
        recyclerWishlist.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerWishlist.setNestedScrollingEnabled(false);

        // Sample data (replace with your real data source)
        wishlistItems = new ArrayList<>();
        wishlistItems.add(new WishlistItem(
                "Jan Sflanaganvik sofa", "Beige", "$599",
                R.drawable.sofa));   // <-- add your image here later
        wishlistItems.add(new WishlistItem(
                "Sverom chair", "Beige", "$400",
                R.drawable.chair));
        wishlistItems.add(new WishlistItem(
                "Kallax chair", "Beige", "$199",
                R.drawable.chair));

        adapter = new WishlistAdapter(this, wishlistItems);
        recyclerWishlist.setAdapter(adapter);

        // Bell icon → open NotificationActivity
        btnBell.setOnClickListener(v -> {
            startActivity(new Intent(
                    WishlistActivity.this,
                    NotificationActivity.class));
        });
    }
}