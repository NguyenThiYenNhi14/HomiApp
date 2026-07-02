package com.yn.homi.ui.profile.profile;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.PropertyName;

public class UserProfile implements Parcelable {
    public String uid;
    public String fullName;
    public String email;
    public String phone; // Khớp với field 'phone' trên Firestore
    public String address;
    public String gender;
    public String dateOfBirth;
    
    @PropertyName("avatarUrl")
    public String avatarUri; // Map avatarUrl từ Firestore vào biến avatarUri

    public UserStats stats;
    public UserPreferences preferences;

    public UserProfile() {
        this.stats = new UserStats();
        this.preferences = new UserPreferences();
    }

    // --- Sub-classes cho Stats và Preferences ---
    public static class UserStats implements Parcelable {
        public int coupons = 0;
        public int points = 0;
        public int views = 0;
        public int wishlists = 0;
        public int lastBirthdayCouponYear = 0;

        public UserStats() {}

        protected UserStats(Parcel in) {
            coupons = in.readInt();
            points = in.readInt();
            views = in.readInt();
            wishlists = in.readInt();
            lastBirthdayCouponYear = in.readInt();
        }

        public static final Creator<UserStats> CREATOR = new Creator<UserStats>() {
            @Override
            public UserStats createFromParcel(Parcel in) { return new UserStats(in); }
            @Override
            public UserStats[] newArray(int size) { return new UserStats[size]; }
        };

        @Override public int describeContents() { return 0; }
        @Override public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(coupons);
            dest.writeInt(points);
            dest.writeInt(views);
            dest.writeInt(wishlists);
            dest.writeInt(lastBirthdayCouponYear);
        }
    }

    public static class UserPreferences implements Parcelable {
        public boolean darkMode = false;
        public String language = "vi";
        public boolean notifications = false;

        public UserPreferences() {}

        protected UserPreferences(Parcel in) {
            darkMode = in.readByte() != 0;
            language = in.readString();
            notifications = in.readByte() != 0;
        }

        public static final Creator<UserPreferences> CREATOR = new Creator<UserPreferences>() {
            @Override
            public UserPreferences createFromParcel(Parcel in) { return new UserPreferences(in); }
            @Override
            public UserPreferences[] newArray(int size) { return new UserPreferences[size]; }
        };

        @Override public int describeContents() { return 0; }
        @Override public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (darkMode ? 1 : 0));
            dest.writeString(language);
            dest.writeByte((byte) (notifications ? 1 : 0));
        }
    }

    // --- Parcelable Implementation cho UserProfile ---
    protected UserProfile(Parcel in) {
        uid = in.readString();
        fullName = in.readString();
        email = in.readString();
        phone = in.readString();
        address = in.readString();
        gender = in.readString();
        dateOfBirth = in.readString();
        avatarUri = in.readString();
        stats = in.readParcelable(UserStats.class.getClassLoader());
        preferences = in.readParcelable(UserPreferences.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(fullName);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(address);
        dest.writeString(gender);
        dest.writeString(dateOfBirth);
        dest.writeString(avatarUri);
        dest.writeParcelable(stats, flags);
        dest.writeParcelable(preferences, flags);
    }

    @Override public int describeContents() { return 0; }

    public static final Creator<UserProfile> CREATOR = new Creator<UserProfile>() {
        @Override
        public UserProfile createFromParcel(Parcel in) { return new UserProfile(in); }
        @Override
        public UserProfile[] newArray(int size) { return new UserProfile[size]; }
    };
}
