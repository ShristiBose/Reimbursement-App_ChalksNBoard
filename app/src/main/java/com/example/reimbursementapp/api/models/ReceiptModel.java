package com.example.reimbursementapp.api.models;

import java.util.Date;

public class ReceiptModel {

    private String receiptId;
    private String requestId;
    private String staffName;
    private String category;
    private Double amount;
    private String description;
    private Date creditedDate;
    private String creditedBy;

    // --- Getters ---
    public String getReceiptId() { return receiptId; }
    public String getRequestId() { return requestId; }
    public String getStaffName() { return staffName; }
    public String getCategory() { return category; }
    public Double getAmount() { return amount; }
    public String getDescription() { return description; }
    public Date getCreditedDate() { return creditedDate; }
    public String getCreditedBy() { return creditedBy; }

    // --- Setters ---
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setCreditedDate(Date creditedDate) { this.creditedDate = creditedDate; }
    public void setCreditedBy(String creditedBy) { this.creditedBy = creditedBy; }
}
