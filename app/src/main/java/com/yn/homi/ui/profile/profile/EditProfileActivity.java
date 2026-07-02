package com.yn.homi.ui.profile.profile;

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

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yn.homi.R;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.widget.ImageButton;

public class EditProfileActivity extends AppCompatActivity {

    private UserProfile profile;
    private Uri selectedAvatarUri = null;
    private Uri cameraUri = null;

    private ImageView ivAvatar;
    private EditText etFullName, etPhone, etEmail, etDob, etAddress;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnSave;
    private FrameLayout flAvatar;

    private static final String CLOUDINARY_CLOUD_NAME = "ddkaekbnb";
    private static final String CLOUDINARY_UPLOAD_PRESET = "Homi_Avatar";

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
        etAddress  = findViewById(R.id.etAddress);
        rgGender   = findViewById(R.id.rgGender);
        rbMale     = findViewById(R.id.rbMale);
        rbFemale   = findViewById(R.id.rbFemale);
        btnSave    = findViewById(R.id.btnSave);
        flAvatar   = findViewById(R.id.flAvatar);

        // Điền thông tin cũ
        etFullName.setText(profile.fullName);
        etPhone.setText(profile.phone);
        etEmail.setText(profile.email);
        etAddress.setText(profile.address);
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

        ImageView icCalendar = findViewById(R.id.icCalendar);
        if (icCalendar != null && icCalendar.getDrawable() != null) {
            icCalendar.getDrawable().setColorFilter(
                    new PorterDuffColorFilter(0xFF757575, PorterDuff.Mode.SRC_IN)
            );
        }
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
                    getPackageName() + ".fileprovider",
                    imageFile
            );
            cameraLauncher.launch(cameraUri);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.msg_camera_error), Toast.LENGTH_SHORT).show();
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
            etFullName.setError(getString(R.string.msg_enter_name));
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
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, getString(R.string.msg_not_logged_in), Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        // LUÔN gọi updateFirestore trước để lưu các trường text ngay lập tức (không chờ ảnh)
        updateFirestore(uid, profile.avatarUri);

        // Nếu có ảnh mới, gọi upload Cloudinary
        if (selectedAvatarUri != null && !selectedAvatarUri.toString().startsWith("http")) {
            uploadAvatarToCloudinary(uid);
        }
    }

    private void uploadAvatarToCloudinary(String uid) {
        new Thread(() -> {
            try {
                java.io.InputStream inputStream = getContentResolver().openInputStream(selectedAvatarUri);
                byte[] imageBytes = new byte[inputStream.available()];
                inputStream.read(imageBytes);
                inputStream.close();

                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(imageBytes, okhttp3.MediaType.parse("image/*"));

                okhttp3.MultipartBody requestBody = new okhttp3.MultipartBody.Builder()
                        .setType(okhttp3.MultipartBody.FORM)
                        .addFormDataPart("file", "avatar.jpg", fileBody)
                        .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("https://api.cloudinary.com/v1_1/" + CLOUDINARY_CLOUD_NAME + "/image/upload")
                        .post(requestBody)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            org.json.JSONObject json = new org.json.JSONObject(responseBody);
                            String secureUrl = json.getString("secure_url");
                            updateAvatarUrlOnly(uid, secureUrl);
                        } catch (Exception e) {
                            Toast.makeText(this, getString(R.string.msg_image_process_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.msg_upload_failed, responseBody), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, getString(R.string.msg_upload_error, e.getMessage()), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateAvatarUrlOnly(String uid, String avatarUrl) {
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("avatarUrl", avatarUrl)
                .addOnSuccessListener(aVoid -> {
                    profile.avatarUri = avatarUrl;
                    Toast.makeText(this, getString(R.string.msg_avatar_updated), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.msg_avatar_save_failed, e.getMessage()), Toast.LENGTH_SHORT).show());
    }

    private void updateFirestore(String uid, String avatarUrl) {
        // Lấy dữ liệu từ giao diện
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String gender = rbFemale.isChecked() ? "Female" : "Male";

        // Chuẩn bị Map để update lên Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("phone", phone);
        updates.put("email", email);
        updates.put("address", address);
        updates.put("dateOfBirth", dob);
        updates.put("gender", gender);

        // Bỏ tham số avatarUrl khỏi Map updates nếu không có ảnh mới được chọn
        // Không ghi đè avatarUrl bằng giá trị cũ nếu không cần thiết
        if (selectedAvatarUri != null && !selectedAvatarUri.toString().startsWith("http")) {
            // Có ảnh mới, sẽ cập nhật qua Cloudinary sau
        } else {
            // Không có ảnh mới, giữ nguyên field trên Firestore
        }

        updates.put("updatedAt", java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật object cục bộ
                    profile.fullName = fullName;
                    profile.phone = phone;
                    profile.email = email;
                    profile.address = address;
                    profile.dateOfBirth = dob;
                    profile.gender = gender;
                    profile.avatarUri = avatarUrl;

                    Toast.makeText(EditProfileActivity.this, getString(R.string.msg_update_success), Toast.LENGTH_SHORT).show();

                    Intent result = new Intent();
                    result.putExtra("UPDATED_PROFILE", profile);
                    setResult(RESULT_OK, result);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, getString(R.string.msg_save_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }
}
