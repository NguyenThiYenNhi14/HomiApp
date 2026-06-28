package com.yn.homi.ui.profile.address;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.yn.homi.R;

import java.util.ArrayList;
import java.util.List;

public class SavedAddressesActivity extends AppCompatActivity {

    public static final String EXTRA_ADDRESS_ID = "address_id";

    private LinearLayout addressListContainer;
    private List<Address> addressList = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    private final ActivityResultLauncher<Intent> addressLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            loadAddressesFromFirestore();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_addresses);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        Toolbar toolbar = findViewById(R.id.toolbar_addresses);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        addressListContainer = findViewById(R.id.address_list_container);
        
        if (userId != null) {
            loadAddressesFromFirestore();
        } else {
            Toast.makeText(this, "Please login to manage addresses", Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.btn_add_address).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAddressActivity.class);
            addressLauncher.launch(intent);
        });
    }

    private void loadAddressesFromFirestore() {
        db.collection("users").document(userId).collection("addresses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    addressList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Address address = doc.toObject(Address.class);
                        if (address != null) {
                            address.setId(doc.getId());
                            addressList.add(address);
                        }
                    }
                    renderAddressList();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load addresses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void renderAddressList() {
        addressListContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (addressList.isEmpty()) {
            showEmptyState();
            return;
        }

        for (Address address : addressList) {
            View card = inflater.inflate(R.layout.item_address_card, addressListContainer, false);

            TextView tvLabel    = card.findViewById(R.id.tv_address_label);
            TextView tvDefault  = card.findViewById(R.id.tv_default_badge);
            TextView tvName     = card.findViewById(R.id.tv_recipient_name);
            TextView tvPhone    = card.findViewById(R.id.tv_recipient_phone);
            TextView tvAddress  = card.findViewById(R.id.tv_full_address);
            TextView btnOptions = card.findViewById(R.id.btn_address_options);

            tvLabel.setText(address.getLabel());
            tvName.setText(address.getRecipientName());
            tvPhone.setText(address.getPhone());
            tvAddress.setText(address.getFullAddress());
            tvDefault.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);

            btnOptions.setOnClickListener(v -> showOptionsMenu(v, address));
            addressListContainer.addView(card);
        }
    }

    private void showOptionsMenu(View anchor, Address address) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Edit");
        if (!address.isDefault()) {
            popup.getMenu().add(0, 2, 1, "Set as Default");
        }
        popup.getMenu().add(0, 3, 2, "Delete");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                Intent intent = new Intent(this, AddAddressActivity.class);
                intent.putExtra("address_data", address);
                addressLauncher.launch(intent);
                return true;
            } else if (id == 2) {
                setDefaultAddress(address);
                return true;
            } else if (id == 3) {
                deleteAddress(address);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void setDefaultAddress(Address selected) {
        WriteBatch batch = db.batch();
        
        // Unset previous default
        for (Address a : addressList) {
            if (a.isDefault()) {
                batch.update(db.collection("users").document(userId).collection("addresses").document(a.getId()), "isDefault", false);
            }
        }
        
        // Set new default
        batch.update(db.collection("users").document(userId).collection("addresses").document(selected.getId()), "isDefault", true);
        
        batch.commit().addOnSuccessListener(aVoid -> {
            loadAddressesFromFirestore();
            Toast.makeText(this, "Default address updated", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteAddress(Address address) {
        db.collection("users").document(userId).collection("addresses").document(address.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    loadAddressesFromFirestore();
                    Toast.makeText(this, "Address deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showEmptyState() {
        TextView empty = new TextView(this);
        empty.setText("No saved addresses yet.\nTap below to add one.");
        empty.setTextColor(0xFF888888);
        empty.setTextSize(14);
        empty.setGravity(android.view.Gravity.CENTER);
        empty.setPadding(0, 80, 0, 80);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        empty.setLayoutParams(params);
        addressListContainer.addView(empty);
    }

    private List<Address> getSampleAddresses() {
        List<Address> list = new ArrayList<>();
        list.add(new Address("1", "Home", "Kevin Gilbert", "+84 901 234 567",
                "123 Nguyen Hue Blvd", "Ben Nghe Ward", "District 1",
                "Ho Chi Minh City", true));
        list.add(new Address("2", "Work", "Kevin Gilbert", "+84 901 234 567",
                "456 Le Loi St", "Ben Thanh Ward", "District 1",
                "Ho Chi Minh City", false));
        return list;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
