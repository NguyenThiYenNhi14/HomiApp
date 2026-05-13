package com.yn.homi.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.yn.homi.R;
// ← đổi thành package HomeActivity thực tế của nhóm bạn
import com.yn.homi.home.HomeActivity;

public class OrderSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        Button btnViewOrder  = findViewById(R.id.btnViewOrder);
        Button btnBackToHome = findViewById(R.id.btnBackToHome);

        btnViewOrder.setOnClickListener(v -> {
            // TODO: mở màn hình Order History / Order Detail
        });

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            // Xóa toàn bộ back stack: Cart → Checkout → Success đều bị clear
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}