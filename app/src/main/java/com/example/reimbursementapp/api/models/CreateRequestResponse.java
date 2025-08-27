package com.example.reimbursementapp.api.models;

public class CreateRequestResponse {

    private String message;
    private String id;

    // --- Getters ---
    public String getMessage() { return message; }
    public String getId() { return id; }

    // --- Setters ---
    public void setMessage(String message) { this.message = message; }
    public void setId(String id) { this.id = id; }
}
