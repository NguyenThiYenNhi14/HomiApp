package com.yn.homi.utils;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

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

    public void sendOTPToPhone(String phoneNumber, OTPCallback callback) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        callback.onVerificationSuccess();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e("OTP_DEBUG", "Firebase Error: " + e.getMessage(), e);
                        // Trả về lỗi gốc từ Firebase để biết chính xác nguyên nhân
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

    public void sendOTPToEmail(String email, OTPCallback callback) {
        callback.onCodeSent("EMAIL_VERIFY_ID", null);
    }
}
