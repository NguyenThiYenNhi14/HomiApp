package com.yn.homi.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME      = "Homi";
    private static final String KEY_TERMS      = "terms_accepted";
    private static final String KEY_USER_NAME  = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_NOTIF      = "notifications_enabled";
    private static final String KEY_AVATAR_URI = "avatar_uri";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ADDRESS = "user_address";

    private static SharedPrefManager instance;
    private final SharedPreferences prefs;

    private SharedPrefManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // ===== TERMS =====

    public void setTermsAccepted(boolean accepted) {
        prefs.edit().putBoolean(KEY_TERMS, accepted).apply();
    }

    public boolean isTermsAccepted() {
        return prefs.getBoolean(KEY_TERMS, false);
    }

    // ===== USER PROFILE (DEPRECATED: Use FirestoreRepository instead) =====

    /** @deprecated Use FirestoreRepository.saveUserProfile instead */
    @Deprecated
    public void saveProfile(String name, String email, String avatarUri) {
        prefs.edit()
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_AVATAR_URI, avatarUri)
                .apply();
    }

    /** @deprecated Use FirestoreRepository.getUserProfile instead */
    @Deprecated
    public String getAvatarUri() {
        return prefs.getString(KEY_AVATAR_URI, "");
    }

    /** @deprecated Use FirestoreRepository.getUserProfile instead */
    @Deprecated
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Kevin Gilbert");
    }

    /** @deprecated Use FirestoreRepository.getUserProfile instead */
    @Deprecated
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "kevingilbert@gmail.com");
    }

    // ===== NOTIFICATIONS =====

    /** Lưu trạng thái bật/tắt notification */
    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIF, enabled).apply();
    }

    public void setLoggedIn(boolean isLoggedIn, int userId, String username, String email, String role) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, username)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "USER");
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public void saveAddress(String address) {
        prefs.edit().putString(KEY_USER_ADDRESS, address).apply();
    }

    public String getUserAddress() {
        return prefs.getString(KEY_USER_ADDRESS, "Ký túc xá khu B, ĐHQG, Dĩ An, Bình Dương");
    }

    /** Lấy trạng thái notification, mặc định bật (true) */
    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIF, true);
    }

    public void clearUserSession() {
        prefs.edit()
                .remove(KEY_IS_LOGGED_IN)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_AVATAR_URI)
                .remove(KEY_USER_ROLE)
                .remove(KEY_NOTIF)
                .apply();
    }
}
