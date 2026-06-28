package com.yn.homi.ui.profile.about;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.yn.homi.R;
import com.yn.homi.data.local.SharedPrefManager;

public class TermsConditionsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private CheckBox checkboxAgree;
    private Button btnAccept;
    private Button btnDecline;

    private final LinearLayout[] sectionHeaders  = new LinearLayout[8];
    private final TextView[]     sectionContents = new TextView[8];
    private final ImageView[]    sectionIcons    = new ImageView[8];
    private final View[]         sectionDividers = new View[8];
    private final boolean[]      sectionExpanded = new boolean[8];

    private final int[] sectionLayoutIds = {
            R.id.section1, R.id.section2, R.id.section3, R.id.section4,
            R.id.section5, R.id.section6, R.id.section7, R.id.section8
    };

    private final String[] sectionTitles = {
            "Acceptance of Terms",
            "Products & Orders",
            "Payment Policy",
            "Delivery & Shipping",
            "Returns & Refunds",
            "Privacy & Data",
            "Intellectual Property",
            "Limitation of Liability"
    };

    private final String[] sectionBodies = {
            "By downloading, installing, or using the Homi application, you agree to be bound by these Terms and Conditions. If you do not agree, please do not use the app.\n\nWe reserve the right to update these terms at any time. Continued use after changes are posted constitutes your acceptance. You must be at least 18 years old to place orders.",
            "All products listed on Homi are subject to availability. Product images are representative; actual colors and textures may vary slightly.\n\nPrices are displayed in Vietnamese Dong (VND) and are inclusive of applicable taxes unless stated otherwise. We reserve the right to modify prices without prior notice.\n\nPlacing an order constitutes an offer to purchase. Orders are confirmed only upon receipt of our confirmation notification.",
            "Homi accepts: Credit/Debit Cards (Visa, Mastercard), Bank Transfer, MoMo, ZaloPay, and Cash on Delivery (COD) for eligible orders.\n\nAll online transactions are encrypted and processed through secure payment gateways. We do not store full card details on our servers.\n\nWe reserve the right to cancel orders with suspected fraudulent payment activity.",
            "Standard delivery within Ho Chi Minh City takes 3–5 business days. Delivery to other provinces may take 7–14 business days.\n\nFor large furniture items, our team will contact you to schedule a delivery and assembly appointment.\n\nFree delivery is available for orders over 5,000,000 VND within HCMC. Risk of damage passes to the customer upon delivery and signature.",
            "You may return eligible products within 7 days of delivery, provided items are unused, in original packaging, and accompanied by proof of purchase.\n\nCustom-made or personalized furniture items are non-returnable unless defective.\n\nApproved refunds are processed within 5–10 business days to the original payment method.",
            "We collect personal information (name, address, email, phone) necessary to process your orders and improve your experience.\n\nYour data will not be sold to third parties. We may share necessary information with delivery partners and payment processors solely for order fulfillment.\n\nYou may request deletion of your account and personal data at any time by contacting support.",
            "All content within the Homi application — including logos, product photography, text, UI design, and software code — is the intellectual property of Homi Co., Ltd.\n\nYou may not reproduce, copy, distribute, or use any content from this app for commercial purposes without written permission.",
            "Homi is not liable for indirect, incidental, or consequential damages arising from the use or inability to use the app or products purchased.\n\nOur total liability shall not exceed the amount paid for the specific order giving rise to the claim.\n\nThese Terms are governed by the laws of Vietnam. Disputes shall be resolved through negotiation first, then through competent Vietnamese courts if necessary."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_condition);

        initViews();
        loadSections();
        setupListeners();
    }

    private void initViews() {
        btnBack       = findViewById(R.id.btnBack);
        checkboxAgree = findViewById(R.id.checkboxAgree);
        btnAccept     = findViewById(R.id.btnAccept);
        btnDecline    = findViewById(R.id.btnDecline);

        // Mặc định nút Accept bị mờ, chờ user tick checkbox
        btnAccept.setEnabled(false);
        btnAccept.setAlpha(0.5f);
    }

    private void loadSections() {
        for (int i = 0; i < sectionLayoutIds.length; i++) {
            View root = findViewById(sectionLayoutIds[i]);

            sectionHeaders[i]  = root.findViewById(R.id.layoutSectionHeader);
            sectionContents[i] = root.findViewById(R.id.tvSectionContent);
            sectionIcons[i]    = root.findViewById(R.id.ivExpandIcon);
            sectionDividers[i] = root.findViewById(R.id.dividerSection);

            TextView tvNum   = root.findViewById(R.id.tvSectionNumber);
            TextView tvTitle = root.findViewById(R.id.tvSectionTitle);

            tvNum.setText(String.valueOf(i + 1));
            tvTitle.setText(sectionTitles[i]);
            sectionContents[i].setText(sectionBodies[i]);
            sectionExpanded[i] = false;

            final int idx = i;
            sectionHeaders[i].setOnClickListener(v -> toggleSection(idx));
        }

        // Mở section đầu tiên mặc định
        expandSection(0);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        checkboxAgree.setOnCheckedChangeListener((btn, isChecked) -> {
            btnAccept.setEnabled(isChecked);
            btnAccept.setAlpha(isChecked ? 1.0f : 0.5f);
        });

        btnAccept.setOnClickListener(v -> {
            // FIX: Dùng SharedPrefManager thay vì getSharedPreferences trực tiếp
            // để đồng bộ với cả app, tránh dùng 2 key khác nhau ("HomiPrefs" và "Homi")
            SharedPrefManager.getInstance(this).setTermsAccepted(true);

            // FIX: Nếu màn hình này mở từ Settings thì chỉ cần finish(),
            // không nên clear stack vì user đang dùng app bình thường.
            // Chỉ clear stack khi đây là màn hình onboarding lần đầu.
            finish();
        });

        btnDecline.setOnClickListener(v -> finish());
    }

    private void toggleSection(int i) {
        if (sectionExpanded[i]) {
            collapseSection(i);
        } else {
            // Đóng section đang mở (accordion behavior)
            for (int j = 0; j < sectionExpanded.length; j++) {
                if (j != i && sectionExpanded[j]) collapseSection(j);
            }
            expandSection(i);
        }
    }

    private void expandSection(int i) {
        sectionExpanded[i] = true;
        sectionContents[i].setVisibility(View.VISIBLE);
        sectionDividers[i].setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(sectionIcons[i], "rotation", 0f, 180f)
                .setDuration(250).start();
    }

    private void collapseSection(int i) {
        sectionExpanded[i] = false;
        sectionContents[i].setVisibility(View.GONE);
        sectionDividers[i].setVisibility(View.GONE);
        ObjectAnimator.ofFloat(sectionIcons[i], "rotation", 180f, 0f)
                .setDuration(250).start();
    }
}
