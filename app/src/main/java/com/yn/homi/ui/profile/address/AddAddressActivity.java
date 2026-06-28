// AddAddressActivity.java
package com.yn.homi.ui.profile.address;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.yn.homi.R;

public class AddAddressActivity extends AppCompatActivity {

    private String selectedLabel = "Home";

    private TextView labelHome, labelWork, labelOther;
    private EditText etFullName, etPhone, etStreet, etWard, etDistrict, etCity;
    private SwitchCompat switchDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        // ── Toolbar ──
        Toolbar toolbar = findViewById(R.id.toolbar_add_address);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // ── Nút Back ──
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // ── Ánh xạ view ──
        labelHome     = findViewById(R.id.label_home);
        labelWork     = findViewById(R.id.label_work);
        labelOther    = findViewById(R.id.label_other);
        etFullName    = findViewById(R.id.et_full_name);
        etPhone       = findViewById(R.id.et_phone);
        etStreet      = findViewById(R.id.et_street);
        etWard        = findViewById(R.id.et_ward);
        etDistrict    = findViewById(R.id.et_district);
        etCity        = findViewById(R.id.et_city);
        switchDefault = findViewById(R.id.switch_default);

        // ── Kiểm tra mode: Edit hay Add ──
        int editId = getIntent().getIntExtra(SavedAddressesActivity.EXTRA_ADDRESS_ID, -1);
        if (editId != -1) {
            TextView toolbarTitle = findViewById(R.id.toolbar_title);
            toolbarTitle.setText("Edit Address");
        }

        // ── Label selector ──
        labelHome.setOnClickListener(v  -> selectLabel("Home"));
        labelWork.setOnClickListener(v  -> selectLabel("Work"));
        labelOther.setOnClickListener(v -> selectLabel("Other"));

        // ── Nút Save ──
        Button btnSave = findViewById(R.id.btn_save_address);
        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void selectLabel(String label) {
        selectedLabel = label;

        // Reset tất cả về unselected
        labelHome.setBackgroundResource(R.drawable.bg_label_unselected);
        labelWork.setBackgroundResource(R.drawable.bg_label_unselected);
        labelOther.setBackgroundResource(R.drawable.bg_label_unselected);
        labelHome.setTextColor(0xFF111111);
        labelWork.setTextColor(0xFF111111);
        labelOther.setTextColor(0xFF111111);

        // Set selected
        TextView selectedView = label.equals("Home") ? labelHome :
                label.equals("Work") ? labelWork : labelOther;
        selectedView.setBackgroundColor(0xFF111111);
        selectedView.setTextColor(0xFFFFFFFF);
    }

    private void saveAddress() {
        String name     = etFullName.getText().toString().trim();
        String phone    = etPhone.getText().toString().trim();
        String street   = etStreet.getText().toString().trim();
        String ward     = etWard.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String city     = etCity.getText().toString().trim();

        if (name.isEmpty()) {
            etFullName.setError("Please enter full name");
            etFullName.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Please enter phone number");
            etPhone.requestFocus();
            return;
        }
        if (street.isEmpty()) {
            etStreet.setError("Please enter street address");
            etStreet.requestFocus();
            return;
        }
        if (district.isEmpty()) {
            etDistrict.setError("Please enter district");
            etDistrict.requestFocus();
            return;
        }
        if (city.isEmpty()) {
            etCity.setError("Please enter city");
            etCity.requestFocus();
            return;
        }

        // TODO: lưu vào database / SharedPrefs thực tế ở đây

        Toast.makeText(this, "Address saved!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
