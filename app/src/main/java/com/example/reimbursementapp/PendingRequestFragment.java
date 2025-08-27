package com.example.reimbursementapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reimbursementapp.api.models.RequestModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private ArrayList<RequestModel> requestList;
    private ApiService apiService;
    private String token;

    public PendingRequestFragment(String token) {
        this.token = token;
        this.apiService = ApiClient.getApiService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_request, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewPending);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        requestList = new ArrayList<>();

        adapter = new RequestAdapter(requestList, requireContext(), "teamlead", new RequestAdapter.OnActionListener() {
            @Override
            public void onApprove(RequestModel request) {
                approveRequest(request.getRequestId());
            }

            @Override
            public void onReject(RequestModel request) {
                showRejectDialog(request.getRequestId());
            }

            @Override
            public void onCredit(RequestModel request) {
                // Not used for team lead
            }
        });

        recyclerView.setAdapter(adapter);
        loadPendingRequests();
        return view;
    }

    private void loadPendingRequests() {
        apiService.getPendingRequestsTL().enqueue(new Callback<List<RequestModel>>() { // CORRECTED
            @Override
            public void onResponse(Call<List<RequestModel>> call, Response<List<RequestModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requestList.clear();
                    requestList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Failed to load pending requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RequestModel>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void approveRequest(String requestId) {
        apiService.approveRequestTL(requestId).enqueue(new Callback<Void>() { // CORRECTED
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Request Approved", Toast.LENGTH_SHORT).show();
                    loadPendingRequests(); // Refresh list
                } else {
                    Toast.makeText(requireContext(), "Failed to approve", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRejectDialog(String requestId) {
        final EditText input = new EditText(requireContext());
        input.setHint("Enter rejection remarks");
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reject Request")
                .setView(input)
                .setPositiveButton("Reject", (dialog, which) -> {
                    String remarks = input.getText().toString().trim();
                    if (!remarks.isEmpty()) {
                        rejectRequest(requestId, remarks);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void rejectRequest(String requestId, String remarks) {
        Map<String, String> body = new HashMap<>();
        body.put("remarks", remarks);
        apiService.rejectRequestTL(requestId, body).enqueue(new Callback<Void>() { // CORRECTED
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Request Rejected", Toast.LENGTH_SHORT).show();
                    loadPendingRequests(); // Refresh list
                } else {
                    Toast.makeText(requireContext(), "Failed to reject", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
