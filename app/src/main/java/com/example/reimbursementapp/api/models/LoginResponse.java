// In LoginResponse.java

package com.example.reimbursementapp.api.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    // This field name must match the key in the JSON from the server.
    @SerializedName("token")
    private String token;

    // This will hold the nested user object from the response.
    @SerializedName("user")
    private UserModel user;

    public String getToken() {
        return token;
    }

    public UserModel getUser() {
        return user;
    }
}
