package com.example.reimbursementapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<RequestModel> requestList;
    private Context context;
    private boolean isTeamLeadView;

    public RequestAdapter(List<RequestModel> requestList, Context context, boolean isTeamLeadView) {
        this.requestList = requestList;
        this.context = context;
        this.isTeamLeadView = isTeamLeadView;
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
        holder.tvCategory.setText("Category: " + request.getCategory());
        holder.tvAmount.setText(String.format("Amount: ₹%.2f", request.getAmount()));
        holder.tvStatus.setText("Status: " + request.getStatus());

        // Show remarks if available
        if (request.getRemarks() != null && !request.getRemarks().isEmpty()) {
            holder.tvRemarks.setText("Remarks: " + request.getRemarks());
            holder.tvRemarks.setVisibility(View.VISIBLE);
        } else {
            holder.tvRemarks.setVisibility(View.GONE);
        }

        // Show staff name for team lead view
        if (isTeamLeadView) {
            holder.tvStaffName.setVisibility(View.VISIBLE);
            holder.tvStaffName.setText("Staff: " + request.getStaffName());
        } else {
            holder.tvStaffName.setVisibility(View.GONE);
        }

        holder.btnViewBill.setOnClickListener(v -> {
            if (request.getBillUrl() != null && !request.getBillUrl().isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getBillUrl()));
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Cannot open bill: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "No bill available", Toast.LENGTH_SHORT).show();
            }
        });

        // Add approve/reject buttons for team lead in pending requests only
        if (isTeamLeadView && "Pending".equals(request.getStatus())) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);

            holder.btnApprove.setOnClickListener(v -> {
                if (context instanceof TeamLeadDashboardActivity) {
                    ((TeamLeadDashboardActivity) context).updateRequestStatus(request, "Approved");
                }
            });

            holder.btnReject.setOnClickListener(v -> {
                if (context instanceof TeamLeadDashboardActivity) {
                    ((TeamLeadDashboardActivity) context).updateRequestStatus(request, "Rejected");
                }
            });
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvStatus, tvStaffName, tvRemarks;
        Button btnViewBill, btnApprove, btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvStaffName = itemView.findViewById(R.id.tvStaffName);
            tvRemarks = itemView.findViewById(R.id.tvRemarks);
            btnViewBill = itemView.findViewById(R.id.btnViewBill);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}