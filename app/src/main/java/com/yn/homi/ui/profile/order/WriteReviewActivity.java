package com.yn.homi.ui.profile.order;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.yn.homi.core.BaseActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yn.homi.R;
import com.yn.homi.models.Review;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WriteReviewActivity extends BaseActivity {

    private static final String CLOUDINARY_CLOUD_NAME = "ddkaekbnb";
    private static final String CLOUDINARY_UPLOAD_PRESET = "Homi_Avatar"; 

    private RatingBar ratingBar;
    private EditText etTitle, etBody;
    private AppCompatButton btnSubmit;
    private TextView tvHeader;
    private ImageView imgReview;
    private LinearLayout layoutSelectImage;

    private String productId, productName, orderId, uid;
    private String existingReviewId = null; 
    private Uri selectedImageUri = null;
    private String currentImageUrl = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgReview.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        ratingBar = findViewById(R.id.ratingBar);
        etTitle = findViewById(R.id.etReviewTitle);
        etBody = findViewById(R.id.etReviewBody);
        btnSubmit = findViewById(R.id.btnSubmitReview);
        tvHeader = findViewById(R.id.tvReviewHeader);
        imgReview = findViewById(R.id.imgReview);
        layoutSelectImage = findViewById(R.id.layoutSelectImage);

        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }

        productId = getIntent().getStringExtra("PRODUCT_ID");
        productName = getIntent().getStringExtra("PRODUCT_NAME");
        orderId = getIntent().getStringExtra("ORDER_ID");
        uid = FirebaseAuth.getInstance().getUid();

        if (productName != null && tvHeader != null) {
            tvHeader.setText("Review: " + productName);
        }

        layoutSelectImage.setOnClickListener(v -> openImagePicker());

        checkExistingReview();

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void checkExistingReview() {
        FirebaseFirestore.getInstance()
                .collection("products").document(productId)
                .collection("reviews")
                .whereEqualTo("userId", uid)
                .whereEqualTo("orderId", orderId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        com.google.firebase.firestore.DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        Review existing = doc.toObject(Review.class);
                        if (existing != null) {
                            existingReviewId = doc.getId();
                            if (!existing.isEditable()) {
                                Toast.makeText(this, "Bạn đã đánh giá sản phẩm này (đã quá hạn 10 ngày để sửa)", Toast.LENGTH_LONG).show();
                                btnSubmit.setEnabled(false);
                            } else {
                                tvHeader.setText("Sửa đánh giá: " + productName);
                            }
                            ratingBar.setRating(existing.getRating());
                            etTitle.setText(existing.getTitle());
                            etBody.setText(existing.getBody());
                            currentImageUrl = existing.getImageUrl();
                            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                                Glide.with(this).load(currentImageUrl).into(imgReview);
                            }
                        }
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String title = etTitle.getText().toString().trim();
        String body = etBody.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        if (body.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageToCloudinary();
        } else {
            saveReviewToFirestore(currentImageUrl);
        }
    }

    private void uploadImageToCloudinary() {
        new Thread(() -> {
            try {
                java.io.InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                byte[] imageBytes = new byte[inputStream.available()];
                inputStream.read(imageBytes);
                inputStream.close();

                OkHttpClient client = new OkHttpClient();
                RequestBody fileBody = RequestBody.create(imageBytes, MediaType.parse("image/*"));

                MultipartBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "review_image.jpg", fileBody)
                        .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.cloudinary.com/v1_1/" + CLOUDINARY_CLOUD_NAME + "/image/upload")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String secureUrl = json.getString("secure_url");
                            saveReviewToFirestore(secureUrl);
                        } catch (Exception e) {
                            btnSubmit.setEnabled(true);
                            Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        btnSubmit.setEnabled(true);
                        Toast.makeText(this, "Upload ảnh thất bại: " + responseBody, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Lỗi upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveReviewToFirestore(String imageUrl) {
        float rating = ratingBar.getRating();
        String title = etTitle.getText().toString().trim();
        String body = etBody.getText().toString().trim();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String reviewId = existingReviewId != null ? existingReviewId : db.collection("products").document(productId).collection("reviews").document().getId();

        Review review = new Review();
        review.setReviewId(reviewId);
        review.setProductId(productId);
        review.setUserId(uid);
        review.setReviewerName(FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Anonymous");
        review.setRating((int) rating);
        review.setTitle(title);
        review.setBody(body);
        review.setOrderId(orderId);
        review.setVerifiedBuyer(true);
        review.setImageUrl(imageUrl);
        review.setDate(new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(new java.util.Date())));
        // Sửa lại dòng trên vì dư thừa format
        review.setDate(new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(new java.util.Date()));

        if (existingReviewId == null) {
            review.setCreatedAt(com.google.firebase.Timestamp.now());
        }

        db.collection("products").document(productId).collection("reviews").document(reviewId)
                .set(review)
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(uid).collection("reviews").document(reviewId)
                            .set(review)
                            .addOnSuccessListener(v2 -> {
                                updateProductRating(productId);
                                Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Lỗi lưu đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProductRating(String productId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").document(productId)
                .collection("reviews")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    float sum = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Review r = doc.toObject(Review.class);
                        if (r != null) {
                            sum += r.getRating();
                        }
                    }
                    float average = count > 0 ? sum / count : 0;

                    db.collection("products").document(productId)
                            .update("rating", average, "reviewCount", count);
                });
    }
}
