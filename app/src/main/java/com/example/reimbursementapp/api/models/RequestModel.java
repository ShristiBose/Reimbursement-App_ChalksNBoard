package com.example.reimbursementapp.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class RequestModel {

    @SerializedName("id")
    private String id;

    @SerializedName("staffName")
    private String staffName;

    @SerializedName("staffId")
    private String staffId;

    @SerializedName("amount")
    private Double amount;

    @SerializedName("category")
    private String category;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

    @SerializedName("fileBase64")
    private String fileBase64;

    @SerializedName("fileMimeType")
    private String fileMimeType;

    @SerializedName("remarks")
    private String remarks;

    @SerializedName("teamLeadId")
    private String teamLeadId;

    @SerializedName("receiptId")
    private String receiptId;

    @SerializedName("statusMessage")
    private String statusMessage;

    @SerializedName("requestType")
    private String requestType;

    @SerializedName("creditedBy")
    private String creditedBy;

    @SerializedName("creditedDate")
    private Date creditedDate;

    @SerializedName("timestamp")
    private Date timestamp;

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // RequestModel.java
    public String getRequestId() {
        return id;
    }

    public void setRequestId(String requestId) {
        this.id = requestId;
    }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFileBase64() { return fileBase64; }
    public void setFileBase64(String fileBase64) { this.fileBase64 = fileBase64; }

    public String getFileMimeType() { return fileMimeType; }
    public void setFileMimeType(String fileMimeType) { this.fileMimeType = fileMimeType; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getTeamLeadId() { return teamLeadId; }
    public void setTeamLeadId(String teamLeadId) { this.teamLeadId = teamLeadId; }

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public String getCreditedBy() { return creditedBy; }
    public void setCreditedBy(String creditedBy) { this.creditedBy = creditedBy; }

    public Date getCreditedDate() { return creditedDate; }
    public void setCreditedDate(Date creditedDate) { this.creditedDate = creditedDate; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
