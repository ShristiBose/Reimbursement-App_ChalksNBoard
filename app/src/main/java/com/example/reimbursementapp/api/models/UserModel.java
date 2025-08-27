package com.example.reimbursementapp.api.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

// ✅ ADDED: Implement Parcelable for passing the object between components.
public class UserModel implements Parcelable {

    @SerializedName("id")
    private String id;

    // Use @SerializedName to map the backend's "fullName" to this field.
    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    // ✅ ADDED: This field is critical for login and redirection logic.
    // The name "role" should match the key in the JSON response from the server.
    @SerializedName("role")
    private String role;

    // --- Constructors ---
    public UserModel() {
        // Default constructor required for libraries like Gson/Retrofit.
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }


    // --- Parcelable Implementation ---
    protected UserModel(Parcel in) {
        id = in.readString();
        fullName = in.readString();
        email = in.readString();
        role = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(fullName);
        dest.writeString(email);
        dest.writeString(role);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };
}
