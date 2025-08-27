package com.example.reimbursementapp.api.models;

public class CreateRequestRequest {

    private double amount;
    private String category;
    private String description;
    private String fileBase64;
    private String fileMimeType;

    public CreateRequestRequest(double amount, String category, String description,
                                String fileBase64, String fileMimeType) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.fileBase64 = fileBase64;
        this.fileMimeType = fileMimeType;
    }

    // --- Getters ---
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getFileBase64() { return fileBase64; }
    public String getFileMimeType() { return fileMimeType; }

    // --- Setters ---
    public void setAmount(double amount) { this.amount = amount; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setFileBase64(String fileBase64) { this.fileBase64 = fileBase64; }
    public void setFileMimeType(String fileMimeType) { this.fileMimeType = fileMimeType; }
}
