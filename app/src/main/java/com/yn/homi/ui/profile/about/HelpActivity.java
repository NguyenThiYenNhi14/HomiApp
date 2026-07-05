package com.yn.homi.ui.profile.about;

import android.os.Bundle;
import android.widget.ImageButton;
import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yn.homi.R;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends BaseActivity {

    private RecyclerView recyclerFaq;
    private FaqAdapter faqAdapter;
    private List<FaqItem> faqList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Nút back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        recyclerFaq = findViewById(R.id.recyclerFaq);
        recyclerFaq.setLayoutManager(new LinearLayoutManager(this));

        // Tạo dữ liệu FAQ
        faqList = createFaqData();

        // Gắn adapter
        faqAdapter = new FaqAdapter(faqList);
        recyclerFaq.setAdapter(faqAdapter);
    }

    // ====== DỮ LIỆU CÂU HỎI - BẠN SỬA NỘI DUNG Ở ĐÂY ======
    private List<FaqItem> createFaqData() {
        List<FaqItem> list = new ArrayList<>();

        list.add(new FaqItem(
                "How do I place an order?",
                "Browse our furniture collection, select the item you like, choose your preferred size and color, then tap 'Add to Cart'. When ready, go to your Cart and tap 'Checkout' to complete your purchase."
        ));

        list.add(new FaqItem(
                "What payment methods are accepted?",
                "We accept credit/debit cards (Visa, MasterCard), PayPal, and cash on delivery (COD) for eligible areas."
        ));

        list.add(new FaqItem(
                "How long does delivery take?",
                "Standard delivery takes 5–7 business days. Express delivery (2–3 days) is available for an additional fee. You can track your order in the 'My Orders' section."
        ));

        list.add(new FaqItem(
                "Can I cancel or modify my order?",
                "You can cancel or modify your order within 24 hours of placing it. After that, the order may already be in processing. Please contact our support team for assistance."
        ));

        list.add(new FaqItem(
                "How do I return a product?",
                "We offer a 30-day return policy. The item must be unused and in its original packaging. Go to 'My Orders', select the item, and tap 'Request Return' to start the process."
        ));

        list.add(new FaqItem(
                "How do I contact customer support?",
                "You can reach us via email at support@homi.com or through the live chat feature available Monday–Friday, 9AM–6PM."
        ));

        return list;
    }
}
