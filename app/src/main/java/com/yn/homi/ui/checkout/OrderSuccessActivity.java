package com.yn.homi.ui.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yn.homi.R;

import com.yn.homi.ui.profile.order.MyOrdersActivity;
import com.yn.homi.ui.home.HomeActivity;

public class OrderSuccessActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cart is now cleared in CheckoutActivity.proceedToSuccess() before coming here
        // to ensure the order data is captured before clearing.

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_order_success);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton btnBack  = findViewById(R.id.btnBack);
        Button btnViewOrder  = findViewById(R.id.btnViewOrder);
        Button btnBackToHome = findViewById(R.id.btnBackToHome);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnViewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyOrdersActivity.class);
            startActivity(intent);
            finish();
        });

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

    }
}
