package com.yn.homi.model;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.yn.homi.R;

public class TrackPackageActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_track_package);
        String orderId = getIntent().getStringExtra("ORDER_ID");
        // TODO: show tracking steps using TrackingStep model
        } }