package com.example.reimbursementapp.api.models;

public class LoginResponse {

    private String message; // e.g. "SUCCESS: Login Successful. Token: <jwt>"

    // --- Getter ---
    public String getMessage() { return message; }

    // --- Setter ---
    public void setMessage(String message) { this.message = message; }
}
