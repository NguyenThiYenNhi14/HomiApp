package com.yn.homi.ui.profile.order;

import android.os.Bundle;

import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;

import com.yn.homi.R;

public class TrackPackageActivity extends BaseActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_track_package);
        String orderId = getIntent().getStringExtra("ORDER_ID");
        // TODO: show tracking steps using TrackingStep model
        } }
