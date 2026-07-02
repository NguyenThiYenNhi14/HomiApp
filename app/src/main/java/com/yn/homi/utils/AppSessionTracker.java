package com.yn.homi.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yn.homi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppSessionTracker implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "AppSessionTracker";
    private static final long SESSION_THRESHOLD_MS = 7 * 60 * 1000; // 7 phút
    private static final int POINTS_TO_AWARD = 10;

    private int activeActivities = 0;
    private Activity currentActivity;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable sessionRunnable;
    private boolean pointsAwardedInThisSession = false;

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
        if (activeActivities == 0) {
            Log.d(TAG, "App entered foreground. Checking session...");
            startSessionTimer();
        }
        activeActivities++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {}

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
        activeActivities--;
        if (activeActivities == 0) {
            Log.d(TAG, "App entered background. Timer continues as per requirement.");
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // Nếu tất cả activity bị destroy (app thoát hẳn), dừng timer
        if (activeActivities <= 0) {
            stopSessionTimer();
        }
    }

    private void startSessionTimer() {
        // Nếu đã cộng điểm rồi hoặc timer đang chạy thì không tạo mới
        if (pointsAwardedInThisSession || sessionRunnable != null) return;

        sessionRunnable = () -> {
            awardPoints();
            pointsAwardedInThisSession = true;
            Log.d(TAG, "7 minutes milestone reached!");
        };

        handler.postDelayed(sessionRunnable, SESSION_THRESHOLD_MS);
        Log.d(TAG, "Session timer started for 7 minutes.");
    }

    private void stopSessionTimer() {
        if (sessionRunnable != null) {
            handler.removeCallbacks(sessionRunnable);
            sessionRunnable = null;
            Log.d(TAG, "Session timer stopped because app was closed.");
        }
    }

    private void awardPoints() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "No user logged in. Cannot award points.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid())
                .update("stats.points", FieldValue.increment(POINTS_TO_AWARD))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully awarded 10 points!");
                    if (currentActivity != null) {
                        Toast.makeText(currentActivity, currentActivity.getString(R.string.msg_earn_points, POINTS_TO_AWARD), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error awarding points: " + e.getMessage()));
    }
}
