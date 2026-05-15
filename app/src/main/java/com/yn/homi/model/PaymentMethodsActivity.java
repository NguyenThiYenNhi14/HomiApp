package com.yn.homi.model;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodsActivity extends AppCompatActivity
        implements PaymentMethodAdapter.OnCardActionListener {

    private final List<PaymentCard> cardList = new ArrayList<>();
    private PaymentMethodAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);

        setupToolbar();
        setupRecyclerView();
        loadSampleData();

        findViewById(R.id.btnAddCard).setOnClickListener(v ->
                Toast.makeText(this, "Feature coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rvPaymentMethods);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PaymentMethodAdapter(cardList, this);
        rv.setAdapter(adapter);
    }

    private void loadSampleData() {
        cardList.add(new PaymentCard("1", PaymentCard.PaymentType.MOMO,
                "MoMo", "0909 *** *** 12", "MoMo E-wallet",
                true, Color.parseColor("#FFE0F0")));

        cardList.add(new PaymentCard("2", PaymentCard.PaymentType.ZALOPAY,
                "ZaloPay", "0987 *** *** 34", "ZaloPay E-wallet",
                false, Color.parseColor("#E0F0FF")));

        cardList.add(new PaymentCard("3", PaymentCard.PaymentType.VISA,
                "Visa", "**** **** **** 4242", "Expires 12/27",
                false, Color.parseColor("#FFF3E0")));

        cardList.add(new PaymentCard("4", PaymentCard.PaymentType.BANK,
                "Vietcombank", "1234 **** **** 5678", "Bank Account",
                false, Color.parseColor("#E8F5E9")));

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteCard(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remove payment information")
                .setMessage("Are you sure you want to remove this?")
                .setPositiveButton("Delete", (d, w) -> adapter.removeItem(position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onSelectCard(int position) {
        Toast.makeText(this,
                "Selected: " + cardList.get(position).getDisplayName(),
                Toast.LENGTH_SHORT).show();
    }
}