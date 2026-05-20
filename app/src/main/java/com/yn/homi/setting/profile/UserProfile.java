package com.yn.homi.setting.profile;

import android.os.Parcel;
import android.os.Parcelable;

// Parcelable để có thể truyền object qua Intent
public class UserProfile implements Parcelable {

    public String fullName;
    public String phoneNumber;
    public String email;
    public String gender;
    public String dateOfBirth;
    public String avatarUri;  // đường dẫn ảnh

    // Constructor rỗng
    public UserProfile() {
        fullName    = "";
        phoneNumber = "";
        email       = "";
        gender      = "";
        dateOfBirth = "";
        avatarUri   = "";
    }

    // ---- Parcelable (bắt buộc để truyền qua Intent) ----
    protected UserProfile(Parcel in) {
        fullName    = in.readString();
        phoneNumber = in.readString();
        email       = in.readString();
        gender      = in.readString();
        dateOfBirth = in.readString();
        avatarUri   = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fullName);
        dest.writeString(phoneNumber);
        dest.writeString(email);
        dest.writeString(gender);
        dest.writeString(dateOfBirth);
        dest.writeString(avatarUri);
    }

    @Override public int describeContents() { return 0; }

    public static final Creator<UserProfile> CREATOR = new Creator<UserProfile>() {
        @Override
        public UserProfile createFromParcel(Parcel in) { return new UserProfile(in); }
        @Override
        public UserProfile[] newArray(int size) { return new UserProfile[size]; }
    };
}