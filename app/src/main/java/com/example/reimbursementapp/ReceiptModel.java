package com.example.reimbursementapp;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ReceiptModel {
    private String receiptId;
    private String requestId;
    private String staffId;
    private String staffName;
    private String creditedBy;     // Admin UID
    private Double amount;
    private String category;
    private String remarks;

    @ServerTimestamp
    private Date creditedDate;

    public ReceiptModel() {}

    public String getReceiptId() { return receiptId; }
    public void setReceiptId(String receiptId) { this.receiptId = receiptId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getCreditedBy() { return creditedBy; }
    public void setCreditedBy(String creditedBy) { this.creditedBy = creditedBy; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Date getCreditedDate() { return creditedDate; }
    public void setCreditedDate(Date creditedDate) { this.creditedDate = creditedDate; }
}
