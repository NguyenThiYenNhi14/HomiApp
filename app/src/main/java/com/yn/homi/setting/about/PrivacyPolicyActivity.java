// PrivacyPolicyActivity.java
package com.yn.homi.setting.about;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.yn.homi.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private static final String[][] SECTIONS = {
            {
                    "1",
                    "Information We Collect",
                    "When you use the Homi app, we may collect the following information:\n\n" +
                            "• Full name, email address, and phone number when you create an account\n" +
                            "• Delivery address to process your orders\n" +
                            "• Payment information (encrypted and handled by a secure third party)\n" +
                            "• Product browsing history and order history\n" +
                            "• Device data: device type, operating system, Android ID\n" +
                            "• Approximate location (if you grant permission) to suggest nearby stores"
            },
            {
                    "2",
                    "How We Use Your Information",
                    "Your information is used to:\n\n" +
                            "• Process and deliver your orders\n" +
                            "• Provide customer support when issues arise\n" +
                            "• Send order notifications and promotions (if you opt in)\n" +
                            "• Improve your in-app experience\n" +
                            "• Detect and prevent fraud\n" +
                            "• Comply with legal obligations under Vietnamese law"
            },
            {
                    "3",
                    "Sharing Your Information",
                    "We do not sell your personal information. We only share data with:\n\n" +
                            "• Delivery partners to fulfill your orders\n" +
                            "• Payment service providers (VNPay, MoMo, etc.)\n" +
                            "• Government authorities when legally required\n\n" +
                            "All third parties are bound by confidentiality agreements with Homi."
            },
            {
                    "4",
                    "Data Security",
                    "We apply appropriate measures to protect your data, including:\n\n" +
                            "• Data encryption in transit via HTTPS/TLS\n" +
                            "• Passwords stored in hashed form\n" +
                            "• Role-based internal access controls\n" +
                            "• Regular system monitoring\n\n" +
                            "In the event of a security incident, we will notify you as soon as possible."
            },
            {
                    "5",
                    "Your Rights",
                    "You have the right to:\n\n" +
                            "• Access and review your stored personal information\n" +
                            "• Request correction of inaccurate information\n" +
                            "• Request deletion of your account and associated data\n" +
                            "• Withdraw consent to receive marketing notifications at any time\n" +
                            "• File a complaint if you believe your data has been mishandled\n\n" +
                            "Please contact us using the information below to exercise these rights."
            },
            {
                    "6",
                    "Data Retention",
                    "We retain your information for as long as your account remains active. " +
                            "Once your account is deleted, your data will be erased or anonymized within " +
                            "30 days, unless a longer retention period is required by law."
            },
            {
                    "7",
                    "Children",
                    "The Homi app is not directed at users under the age of 13. We do not knowingly " +
                            "collect information from children. If you believe a child has provided us with " +
                            "their data, please contact us so we can take immediate action."
            },
            {
                    "8",
                    "Changes to This Policy",
                    "We may update this policy from time to time. When significant changes are made, " +
                            "you will be notified via the app or by email. Your continued use of the app " +
                            "after changes take effect constitutes your acceptance of the updated policy."
            }
    };

    private static final int[] SECTION_IDS = {
            R.id.section1, R.id.section2, R.id.section3, R.id.section4,
            R.id.section5, R.id.section6, R.id.section7, R.id.section8
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        // Back button
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Populate sections
        for (int i = 0; i < SECTION_IDS.length; i++) {
            View sectionView = findViewById(SECTION_IDS[i]);

            TextView tvNumber = sectionView.findViewById(R.id.tv_section_number);
            TextView tvTitle  = sectionView.findViewById(R.id.tv_section_title);
            TextView tvBody   = sectionView.findViewById(R.id.tv_section_body);

            tvNumber.setText(SECTIONS[i][0]);
            tvTitle.setText(SECTIONS[i][1]);
            tvBody.setText(SECTIONS[i][2]);
        }
    }
}