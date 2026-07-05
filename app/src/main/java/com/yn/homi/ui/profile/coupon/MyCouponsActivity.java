package com.yn.homi.ui.profile.coupon;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.yn.homi.R;
import com.yn.homi.data.model.Coupon;
import java.util.ArrayList;
import java.util.List;

public class MyCouponsActivity extends BaseActivity {

    private RecyclerView rvCoupons;
    private MyCouponsAdapter adapter;
    private List<Coupon> couponList;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_coupons);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        loadCoupons();
    }

    private void initViews() {
        rvCoupons = findViewById(R.id.rvCoupons);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        couponList = new ArrayList<>();
        adapter = new MyCouponsAdapter(couponList);
        rvCoupons.setLayoutManager(new LinearLayoutManager(this));
        rvCoupons.setAdapter(adapter);
    }

    private void loadCoupons() {
        if (auth.getCurrentUser() == null) {
            layoutEmpty.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("coupons")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    couponList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Coupon coupon = doc.toObject(Coupon.class);
                        if (coupon != null) {
                            coupon.setId(doc.getId());
                            couponList.add(coupon);
                        }
                    }

                    if (couponList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvCoupons.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvCoupons.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
    }
}
