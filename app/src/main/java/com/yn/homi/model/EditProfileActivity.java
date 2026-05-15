package com.yn.homi.model;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.yn.homi.model.UserProfile;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.yn.homi.R;

import java.io.File;
import java.util.Calendar;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.widget.ImageButton;

public class EditProfileActivity extends AppCompatActivity {

    private UserProfile profile;
    private Uri selectedAvatarUri = null;
    private Uri cameraUri = null;

    private ImageView ivAvatar;
    private EditText etFullName, etPhone, etEmail, etDob;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnSave;
    private FrameLayout flAvatar;

    // ---- Launcher mở Gallery ----
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedAvatarUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(ivAvatar);
                }
            });

    // ---- Launcher mở Camera ----
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraUri != null) {
                    selectedAvatarUri = cameraUri;
                    Glide.with(this).load(cameraUri).circleCrop().into(ivAvatar);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Nhận profile
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            profile = getIntent().getParcelableExtra("USER_PROFILE", UserProfile.class);
        } else {
            profile = getIntent().getParcelableExtra("USER_PROFILE");
        }
        if (profile == null) profile = new UserProfile();

        // Ánh xạ view
        ivAvatar   = findViewById(R.id.ivAvatar);
        etFullName = findViewById(R.id.etFullName);
        etPhone    = findViewById(R.id.etPhone);
        etEmail    = findViewById(R.id.etEmail);
        etDob      = findViewById(R.id.etDob);
        rgGender   = findViewById(R.id.rgGender);
        rbMale     = findViewById(R.id.rbMale);
        rbFemale   = findViewById(R.id.rbFemale);
        btnSave    = findViewById(R.id.btnSave);
        flAvatar   = findViewById(R.id.flAvatar);

        // Điền thông tin cũ
        etFullName.setText(profile.fullName);
        etPhone.setText(profile.phoneNumber);
        etEmail.setText(profile.email);
        etDob.setText(profile.dateOfBirth);

        // Set gender radio button
        if ("Female".equals(profile.gender)) {
            rbFemale.setChecked(true);
        } else {
            rbMale.setChecked(true); // mặc định Male
        }

        // Hiển thị ảnh cũ nếu có
        if (profile.avatarUri != null && !profile.avatarUri.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(profile.avatarUri))
                    .circleCrop()
                    .placeholder(R.drawable.icon_account_circle)
                    .into(ivAvatar);
        }

        // Click avatar → chọn ảnh
        flAvatar.setOnClickListener(v -> showPhotoPickerDialog());

        // Click ô ngày sinh → mở DatePicker
        // Người dùng vẫn có thể nhập tay hoặc click icon để mở picker
        etDob.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.icCalendar).setOnClickListener(v -> showDatePicker());

        // Click Save → hiện confirm dialog
        btnSave.setOnClickListener(v -> showConfirmDialog());

        ImageButton icCalendar = findViewById(R.id.icCalendar);
        icCalendar.getDrawable().setColorFilter(
                new PorterDuffColorFilter(0xFF111111, PorterDuff.Mode.SRC_IN)
        );

    }

    // ---- Dialog chọn ảnh (giữa màn hình) ----
    private void showPhotoPickerDialog() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_photo_picker, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        // Bo góc dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent);
        }

        view.setBackgroundResource(R.drawable.bg_dialog_rounded);

        view.findViewById(R.id.tvOpenCamera).setOnClickListener(v -> {
            openCamera();
            dialog.dismiss();
        });

        view.findViewById(R.id.tvSelectGallery).setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
            dialog.dismiss();
        });

        dialog.show();
    }

    // ---- Mở Camera ----
    private void openCamera() {
        try {
            File imageFile = File.createTempFile(
                    "avatar_", ".jpg",
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            );
            cameraUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    imageFile
            );
            cameraLauncher.launch(cameraUri);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot open camera", Toast.LENGTH_SHORT).show();
        }
    }

    // ---- DatePicker ----
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        // Nếu đã có ngày nhập tay thì parse để hiện đúng tháng
        String current = etDob.getText().toString().trim();
        if (!current.isEmpty() && current.matches("\\d{2}/\\d{2}/\\d{4}")) {
            try {
                String[] parts = current.split("/");
                cal.set(Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[0]));
            } catch (Exception ignored) {}
        }

        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String date = String.format("%02d/%02d/%04d", day, month + 1, year);
                    etDob.setText(date);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ---- Confirm Dialog trước khi Save ----
    private void showConfirmDialog() {
        // Validate trước
        String name = etFullName.getText().toString().trim();
        if (name.isEmpty()) {
            etFullName.setError("Please enter your name");
            etFullName.requestFocus();
            return;
        }

        // Hiện BottomSheet confirm (giống logout)
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_confirm_save, null);
        bottomSheet.setContentView(view);

        view.findViewById(R.id.btnConfirmYes).setOnClickListener(v -> {
            bottomSheet.dismiss();
            saveProfile(); // thực hiện lưu
        });

        view.findViewById(R.id.btnConfirmCancel).setOnClickListener(v ->
                bottomSheet.dismiss()
        );

        bottomSheet.show();
    }

    // ---- Lưu và trả kết quả ----
    private void saveProfile() {
        profile.fullName    = etFullName.getText().toString().trim();
        profile.phoneNumber = etPhone.getText().toString().trim();
        profile.email       = etEmail.getText().toString().trim();
        profile.dateOfBirth = etDob.getText().toString().trim();
        profile.gender      = rbFemale.isChecked() ? "Female" : "Male";

        if (selectedAvatarUri != null) {
            profile.avatarUri = selectedAvatarUri.toString();
        }

        Intent result = new Intent();
        result.putExtra("UPDATED_PROFILE", profile);
        setResult(RESULT_OK, result);
        finish();
    }
}