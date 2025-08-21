package com.example.reimbursementapp;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class RequestModel {
    private String staffId;
    private String staffName;
    private String category;
    private double amount;
    private String description;
    private String billUrl;
    private String status;
    private String requestId;
    private String remarks;

    @ServerTimestamp
    private Date timestamp;

    public RequestModel() {
        // Firestore requires empty constructor
    }

    // Getters and Setters
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBillUrl() { return billUrl; }
    public void setBillUrl(String billUrl) { this.billUrl = billUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}