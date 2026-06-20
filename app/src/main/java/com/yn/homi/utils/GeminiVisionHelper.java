package com.yn.homi.utils;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GeminiVisionHelper {

    private static final String TAG = "GEMINI";
    private static final String API_KEY = "AIzaSyB3em35Yobt1cFtQeARqgOa6G9RWBQ7pqs"; // Thay bằng key mới của bạn!
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public interface OnAnalysisComplete {
        void onSuccess(FurnitureFeatures features);
        void onError(String error);
    }

    /**
     * FurnitureFeatures chứa các field khớp với Firestore của bạn:
     * - colors    → match field "colors" (array of strings như "Off-White", "Beige")
     * - materials → match field "materials" (array of strings như "Velvet", "Solid Wood")
     * - keywords  → match từ trong field "name" (như "sofa", "chair", "table")
     * - category  → dùng để filter phụ
     */
    public static class FurnitureFeatures {
        public String category;       // Loại đồ nội thất: sofa, chair, table, bed...
        public List<String> colors;   // Màu sắc: "Off-White", "Beige", "Brown"...
        public List<String> materials;// Chất liệu: "Velvet", "Solid Wood", "Fabric"...
        public List<String> keywords; // Từ khóa cho tìm theo name: "curved", "upholstered"...

        @Override
        public String toString() {
            return "category=" + category +
                    ", colors=" + colors +
                    ", materials=" + materials +
                    ", keywords=" + keywords;
        }
    }

    public static void analyzeImage(Bitmap bitmap, OnAnalysisComplete callback) {
        new Thread(() -> {
            try {
                String base64Image = bitmapToBase64(bitmap);

                // Prompt yêu cầu Gemini trả về đúng format khớp với Firestore
                String prompt = "Analyze this furniture image. Return ONLY a valid JSON object (no markdown, no explanation) with these exact keys:\n" +
                        "{\n" +
                        "  \"category\": \"one of: sofa, chair, table, bed, cabinet, shelf, desk, wardrobe, lamp, rug\",\n" +
                        "  \"colors\": [\"list of color names in English, Title Case, e.g. Off-White, Beige, Brown, Gray, Black, White, Blue, Green\"],\n" +
                        "  \"materials\": [\"list of materials in English, Title Case, e.g. Velvet, Solid Wood, Fabric, Leather, Metal, Glass, Rattan\"],\n" +
                        "  \"keywords\": [\"2-4 descriptive words in lowercase English, e.g. curved, upholstered, modern, minimalist, vintage\"]\n" +
                        "}";

                JSONObject imagePart = new JSONObject()
                        .put("inline_data", new JSONObject()
                                .put("mime_type", "image/jpeg")
                                .put("data", base64Image));

                JSONObject textPart = new JSONObject().put("text", prompt);
                JSONObject content = new JSONObject()
                        .put("parts", new JSONArray().put(imagePart).put(textPart));
                JSONObject requestBodyJson = new JSONObject()
                        .put("contents", new JSONArray().put(content));

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(requestBodyJson.toString(), mediaType);

                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    ResponseBody responseBody = response.body();
                    String responseBodyText = responseBody != null ? responseBody.string() : "";

                    if (!response.isSuccessful()) {
                        Log.e(TAG, "HTTP Error " + response.code() + ": " + responseBodyText);
                        callback.onError("Lỗi kết nối AI: " + response.code());
                        return;
                    }

                    FurnitureFeatures features = parseResponse(responseBodyText);
                    if (features == null) {
                        callback.onError("Không thể nhận diện đồ nội thất trong ảnh.");
                    } else {
                        Log.d(TAG, "Recognized: " + features.toString());
                        callback.onSuccess(features);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage(), e);
                callback.onError("Lỗi kết nối: " + e.getMessage());
            }
        }).start();
    }

    private static FurnitureFeatures parseResponse(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            if (!json.has("candidates")) {
                Log.e(TAG, "No candidates in response: " + responseBody);
                return null;
            }

            JSONObject candidate = json.getJSONArray("candidates").getJSONObject(0);

            // Kiểm tra finish reason
            String finishReason = candidate.optString("finishReason", "STOP");
            if (!finishReason.equals("STOP")) {
                Log.w(TAG, "Finish reason: " + finishReason);
                if (finishReason.equals("SAFETY")) return null;
            }

            String text = candidate
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim();

            // Xóa markdown nếu Gemini vẫn trả về
            text = text.replaceAll("(?s)```(json)?\\s*", "").replaceAll("(?s)\\s*```", "").trim();
            Log.d(TAG, "Gemini raw response: " + text);

            JSONObject result = new JSONObject(text);
            FurnitureFeatures f = new FurnitureFeatures();

            f.category = result.optString("category", "").toLowerCase().trim();

            f.colors = jsonArrayToList(result.optJSONArray("colors"));
            f.materials = jsonArrayToList(result.optJSONArray("materials"));
            f.keywords = jsonArrayToList(result.optJSONArray("keywords"));

            // Nếu thiếu data thì return null
            if (f.colors.isEmpty() && f.materials.isEmpty()) {
                Log.w(TAG, "Gemini returned empty colors and materials");
                return null;
            }

            return f;

        } catch (Exception e) {
            Log.e(TAG, "Parse error: " + e.getMessage());
            return null;
        }
    }

    private static List<String> jsonArrayToList(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            try {
                String val = array.getString(i).trim();
                if (!val.isEmpty()) list.add(val);
            } catch (Exception ignored) {}
        }
        return list;
    }

    private static String bitmapToBase64(Bitmap bitmap) {
        int maxSize = 512;
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        Bitmap resized = bitmap;
        if (w > maxSize || h > maxSize) {
            float ratio = (float) w / h;
            if (w > h) {
                resized = Bitmap.createScaledBitmap(bitmap, maxSize, (int) (maxSize / ratio), true);
            } else {
                resized = Bitmap.createScaledBitmap(bitmap, (int) (maxSize * ratio), maxSize, true);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }
}