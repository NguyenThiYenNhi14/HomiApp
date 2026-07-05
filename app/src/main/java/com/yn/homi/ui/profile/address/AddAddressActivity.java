// AddAddressActivity.java
package com.yn.homi.ui.profile.address;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.yn.homi.R;

import java.io.Serializable;

public class AddAddressActivity extends BaseActivity {

    private String selectedLabel = "Home"; // Internally keep keys consistent or use string resources

    private TextView labelHome, labelWork, labelOther;
    private EditText etFullName, etPhone, etStreet, etWard, etDistrict, etCity;
    private SwitchCompat switchDefault;
    private Address editingAddress;
    private boolean isEditMode = false;

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
        editingAddress = (Address) getIntent().getSerializableExtra("address_data");
        isEditMode = editingAddress != null;

        if (isEditMode) {
            TextView toolbarTitle = findViewById(R.id.toolbar_title);
            toolbarTitle.setText(R.string.title_edit_address);

            // Điền sẵn dữ liệu
            etFullName.setText(editingAddress.getRecipientName());
            etPhone.setText(editingAddress.getPhone());
            etStreet.setText(editingAddress.getStreet());
            etWard.setText(editingAddress.getWard());
            etDistrict.setText(editingAddress.getDistrict());
            etCity.setText(editingAddress.getCity());
            switchDefault.setChecked(editingAddress.isDefault());
            selectLabel(editingAddress.getLabel());
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
            etFullName.setError(getString(R.string.err_enter_full_name));
            etFullName.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError(getString(R.string.err_enter_phone));
            etPhone.requestFocus();
            return;
        }
        if (street.isEmpty()) {
            etStreet.setError(getString(R.string.err_enter_street));
            etStreet.requestFocus();
            return;
        }
        if (district.isEmpty()) {
            etDistrict.setError(getString(R.string.err_enter_district));
            etDistrict.requestFocus();
            return;
        }
        if (city.isEmpty()) {
            etCity.setError(getString(R.string.err_enter_city));
            etCity.requestFocus();
            return;
        }

        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.firestore.CollectionReference addressesRef =
                db.collection("users").document(uid).collection("addresses");

        boolean setAsDefault = switchDefault.isChecked();

        Runnable saveAction = () -> {
            Address address = new Address(
                    null, selectedLabel, name, phone, street, ward, district, city, setAsDefault
            );

            com.google.firebase.firestore.DocumentReference docRef;
            if (isEditMode && editingAddress.getId() != null) {
                docRef = addressesRef.document(editingAddress.getId());
            } else {
                docRef = addressesRef.document(); // tạo ID mới tự động
            }

            docRef.set(address)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, R.string.msg_address_saved, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        };

        if (setAsDefault) {
            // Bỏ default của các địa chỉ khác trước khi set địa chỉ này làm default
            addressesRef.get().addOnSuccessListener(snapshot -> {
                com.google.firebase.firestore.WriteBatch batch = db.batch();
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                    if (!doc.getId().equals(isEditMode ? editingAddress.getId() : "")) {
                        batch.update(doc.getReference(), "isDefault", false);
                    }
                }
                batch.commit().addOnSuccessListener(v -> saveAction.run());
            });
        } else {
            saveAction.run();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
