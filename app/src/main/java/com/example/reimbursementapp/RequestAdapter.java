package com.example.reimbursementapp;

import com.example.reimbursementapp.api.models.ReceiptModel;
import com.example.reimbursementapp.api.models.RequestModel;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    public interface OnActionListener {
        void onApprove(RequestModel request);
        void onReject(RequestModel request);
        void onCredit(RequestModel request);
    }

    private final List<RequestModel> requestList;
    private final Context context;
    private final String mode;
    private final OnActionListener actionListener;
    private static final String TAG = "RequestAdapter";

    public RequestAdapter(List<RequestModel> requestList,
                          Context context,
                          String mode,
                          OnActionListener actionListener) {
        this.requestList = requestList;
        this.context = context;
        this.mode = mode == null ? "" : mode.toLowerCase();
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RequestModel request = requestList.get(position);

        // --- Setup TextViews ---
        holder.tvCategory.setText("Category: " + safeText(request.getCategory()));
        holder.tvAmount.setText("Amount: " +
                (request.getAmount() != null ? String.format(Locale.US, "₹%.2f", request.getAmount()) : "N/A"));

        String statusText = request.getStatusMessage() != null && !request.getStatusMessage().isEmpty()
                ? request.getStatusMessage()
                : request.getStatus();
        holder.tvStatus.setText("Status: " + safeText(statusText));

        if (request.getRemarks() != null && !request.getRemarks().isEmpty()) {
            holder.tvRemarks.setText("Remarks: " + request.getRemarks());
            holder.tvRemarks.setVisibility(View.VISIBLE);
        } else {
            holder.tvRemarks.setVisibility(View.GONE);
        }

        holder.tvStaffName.setText(request.getStaffName() != null ? "Staff: " + request.getStaffName() : "");
        holder.tvStaffName.setVisibility(request.getStaffName() != null ? View.VISIBLE : View.GONE);


        holder.btnApprove.setVisibility(View.GONE);
        holder.btnReject.setVisibility(View.GONE);
        holder.btnCredit.setVisibility(View.GONE);
        holder.btnViewReceipt.setVisibility(View.GONE);


        holder.btnViewBill.setVisibility(View.VISIBLE);
        holder.btnViewBill.setOnClickListener(v -> viewBill(request));


        if (request.getCreditedDate() != null || equalsIgnoreCaseSafe(request.getStatus(), "Credited")) {
            holder.btnViewReceipt.setVisibility(View.VISIBLE);
            holder.btnViewReceipt.setOnClickListener(v -> fetchAndViewReceipt(request));
            return;
        }


        if (actionListener != null) {
            String role = mode;
            String status = request.getStatus() != null ? request.getStatus().toLowerCase() : "";

            if ("teamlead".equals(role)) {
                if (status.contains("pending")) {
                    holder.btnApprove.setVisibility(View.VISIBLE);
                    holder.btnReject.setVisibility(View.VISIBLE);
                    holder.btnApprove.setOnClickListener(v -> actionListener.onApprove(request));
                    holder.btnReject.setOnClickListener(v -> actionListener.onReject(request));
                }
            } else if ("admin-staff".equals(role) || "admin-teamlead".equals(role)) {
                boolean isApprovedByTL = status.contains("approved");
                boolean isTeamLeadOwnRequest = request.getStaffId() != null
                        && request.getTeamLeadId() != null
                        && request.getStaffId().equals(request.getTeamLeadId());
                boolean isPendingTLRequest = isTeamLeadOwnRequest && status.contains("pending");

                if (isApprovedByTL || isPendingTLRequest) {
                    holder.btnCredit.setVisibility(View.VISIBLE);
                    holder.btnReject.setVisibility(View.VISIBLE);
                    holder.btnCredit.setOnClickListener(v -> actionListener.onCredit(request));
                    holder.btnReject.setOnClickListener(v -> actionListener.onReject(request));
                }
            }
        }
    }



    private void viewBill(RequestModel request) {
        if (request.getFileBase64() == null || request.getFileBase64().isEmpty()) {
            Toast.makeText(context, "No bill available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            byte[] fileBytes = android.util.Base64.decode(request.getFileBase64(), android.util.Base64.DEFAULT);
            String mime = request.getFileMimeType();
            String ext = ".tmp";
            if (mime != null) {
                if (mime.contains("pdf")) ext = ".pdf";
                else if (mime.contains("png")) ext = ".png";
                else if (mime.contains("jpeg") || mime.contains("jpg")) ext = ".jpg";
            }

            File tempFile = File.createTempFile("bill_", ext, context.getCacheDir());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(fileBytes);
            }

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", tempFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mime != null ? mime : "*/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "No app available to view this file type", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot open bill", e);
            Toast.makeText(context, "Cannot open bill: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAndViewReceipt(RequestModel request) {
        ApiService api = ApiClient.getAuthenticatedApiService(context);

        if (request.getBillId() == null || request.getBillId().isEmpty()) {
            Toast.makeText(context, "Cannot fetch receipt: invalid bill ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String requestType;
        if (request.getRequestType() != null) {
            requestType = request.getRequestType().toLowerCase();
        } else if ("teamlead".equals(mode)) {
            requestType = "teamlead";
        } else {
            requestType = "staff";
        }

        Toast.makeText(context, "Mode=" + mode + ", RequestType=" + requestType, Toast.LENGTH_LONG).show();
        boolean isUserRole = "staff".equals(mode) || "teamlead".equals(mode);

        Call<ReceiptModel> call;

        if (isUserRole) {

            if ("teamlead".equals(requestType)) {
                call = api.getTeamLeadReceiptUser(request.getBillId());
                Log.d(TAG, "Fetching TeamLead receipt (User API) for billId=" + request.getBillId());
            } else {
                call = api.getStaffReceiptUser(request.getBillId());
                Log.d(TAG, "Fetching Staff receipt (User API) for billId=" + request.getBillId());
            }
        } else {

            if ("teamlead".equals(requestType)) {
                call = api.getTeamLeadReceiptAdmin(request.getBillId());
                Log.d(TAG, "Fetching TeamLead receipt (Admin API) for billId=" + request.getBillId());
            } else {
                call = api.getStaffReceipt(request.getBillId());
                Log.d(TAG, "Fetching Staff receipt (Admin API) for billId=" + request.getBillId());
            }
        }

        call.enqueue(new retrofit2.Callback<ReceiptModel>() {
            @Override
            public void onResponse(Call<ReceiptModel> call, retrofit2.Response<ReceiptModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    generateAndViewReceipt(response.body());
                } else {
                    Toast.makeText(context, "Failed to fetch receipt: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Receipt fetch failed: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ReceiptModel> call, Throwable t) {
                Toast.makeText(context, "Error fetching receipt: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Receipt fetch error", t);
            }
        });
    }


    private void generateAndViewReceipt(ReceiptModel receipt) {
        try {
            File pdfFile = new File(context.getCacheDir(), "receipt_" + receipt.getReceiptId() + ".pdf");
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("REIMBURSEMENT RECEIPT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addTableRow(table, "Receipt ID:", receipt.getReceiptId());
            addTableRow(table, "Request ID:", receipt.getRequestId());
            addTableRow(table, "Staff Name:", receipt.getStaffName());
            addTableRow(table, "Category:", receipt.getCategory());
            addTableRow(table, "Amount:", receipt.getAmount() != null ?
                    String.format(Locale.US, "₹%.2f", receipt.getAmount()) : "N/A");
            addTableRow(table, "Description:", receipt.getDescription());
            addTableRow(table, "Credited Date:", receipt.getCreditedDate() != null ?
                    new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(receipt.getCreditedDate()) : "N/A");
            addTableRow(table, "Credited By (Admin):", receipt.getCreditedBy());

            document.add(table);
            document.close();

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "Error generating receipt", e);
            Toast.makeText(context, "Error generating receipt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    private String safeText(String text) {
        return text != null ? text : "N/A";
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvStatus, tvStaffName, tvRemarks;
        Button btnViewBill, btnApprove, btnReject, btnCredit, btnViewReceipt;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.txtCategory);
            tvAmount = itemView.findViewById(R.id.txtAmount);
            tvStatus = itemView.findViewById(R.id.txtStatus);
            tvStaffName = itemView.findViewById(R.id.txtName);
            tvRemarks = itemView.findViewById(R.id.txtRemarks);
            btnViewBill = itemView.findViewById(R.id.btnViewBill);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCredit = itemView.findViewById(R.id.btnCredit);
            btnViewReceipt = itemView.findViewById(R.id.btnViewReceipt);
        }
    }

    private boolean equalsIgnoreCaseSafe(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }
}
