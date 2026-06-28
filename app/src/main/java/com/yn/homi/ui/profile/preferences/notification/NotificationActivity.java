package com.yn.homi.ui.profile.preferences.notification;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ImageView btnBack = findViewById(R.id.btnBack);
        RecyclerView recycler = findViewById(R.id.recyclerNotifications);

        // Back arrow goes to previous screen
        btnBack.setOnClickListener(v -> finish());

        // Sample notifications (add your real data here)
        List<NotificationItem> list = new ArrayList<>();
        String desc = "Lorem Ipsum is simply dummy text of the printing industry.";
        list.add(new NotificationItem(
                "Comfort furniture chairs", desc, "25 min",
                R.drawable.chair));
        list.add(new NotificationItem(
                "Table lamp lighting", desc, "35 min",
                R.drawable.lamp));
        list.add(new NotificationItem(
                "Chair table living room", desc, "55 min",
                R.drawable.sofa));
        list.add(new NotificationItem(
                "Bean bag chairs paper", desc, "1 hr",
                R.drawable.chair));
        list.add(new NotificationItem(
                "Loveseat couch furniture", desc, "1 hr",
                R.drawable.sofa));
        list.add(new NotificationItem(
                "Eames lounge chair", desc, "2 day",
                R.drawable.chair));

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new NotificationAdapter(this, list));
    }
}
