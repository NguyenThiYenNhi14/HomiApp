package com.yn.homi.utils;

import android.app.Activity;
import androidx.annotation.NonNull;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

/**
 * Senior Developer Helper: Class quản lý tập trung logic OTP (SMS và Email)
 */
public class OTPManager {
    private FirebaseAuth mAuth;
    private Activity activity;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    public interface OTPCallback {
        void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token);
        void onVerificationSuccess();
        void onFailure(String error);
    }

    public OTPManager(Activity activity) {
        this.activity = activity;
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Gửi SMS OTP thực tế qua Firebase Phone Auth
     */
    public void sendOTPToPhone(String phoneNumber, OTPCallback callback) {
        // Chuẩn hóa số điện thoại Việt Nam (+84)
        String formattedPhone = phoneNumber;
        if (formattedPhone.startsWith("0")) {
            formattedPhone = "+84" + formattedPhone.substring(1);
        } else if (!formattedPhone.startsWith("+")) {
            formattedPhone = "+84" + formattedPhone;
        }

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(formattedPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Trường hợp tự động xác thực (Instant Verification)
                        callback.onVerificationSuccess();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        callback.onFailure(e.getLocalizedMessage());
                    }

                    @Override
                    public void onCodeSent(@NonNull String vId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = vId;
                        resendToken = token;
                        callback.onCodeSent(vId, token);
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /**
     * Gửi OTP qua Email (Sử dụng backend API hoặc Firebase)
     * Note: Firebase mặc định gửi link, gửi code cần Backend/Cloud Functions
     */
    public void sendOTPToEmail(String email, OTPCallback callback) {
        // Giả lập logic gửi thành công để bạn tích hợp API Backend sau này
        // TODO: Implement OkHttp/Retrofit call to your backend
        callback.onCodeSent("EMAIL_VERIFY_ID", null);
    }

    /**
     * Gửi lại mã OTP (Sử dụng resendToken để tối ưu quota)
     */
    public void resendOTP(String phoneNumber, PhoneAuthProvider.ForceResendingToken token, OTPCallback callback) {
        // Logic tương tự sendOTPToPhone nhưng thêm .setForceResendingToken(token)
        // Sẽ triển khai khi cần Resend
    }
}
