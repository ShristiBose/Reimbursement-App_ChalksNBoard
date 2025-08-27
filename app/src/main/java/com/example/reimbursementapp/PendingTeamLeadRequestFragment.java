package com.example.reimbursementapp;

import android.app.AlertDialog;
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

public class PendingTeamLeadRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private ArrayList<RequestModel> requestList = new ArrayList<>();
    private String token;
    private ApiService apiService;

    public PendingTeamLeadRequestFragment(String token) {
        this.token = token;
        this.apiService = ApiClient.getApiService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_request, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewPending);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RequestAdapter(requestList, getContext(), "admin", new RequestAdapter.OnActionListener() {
            @Override
            public void onApprove(RequestModel request) {
                // This role uses Credit, not Approve
            }

            @Override
            public void onReject(RequestModel request) {
                showRejectDialog(request);
            }

            @Override
            public void onCredit(RequestModel request) {
                creditRequest(request);
            }
        });
        recyclerView.setAdapter(adapter);
        loadPendingRequests();
        return view;
    }

    private void loadPendingRequests() {
        // CORRECTED: Token removed from call
        apiService.getPendingTeamLeadRequests().enqueue(new Callback<List<RequestModel>>() {
            @Override
            public void onResponse(Call<List<RequestModel>> call, Response<List<RequestModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requestList.clear();
                    requestList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load team lead requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RequestModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void creditRequest(RequestModel request) {
        // CORRECTED: Token removed from call
        apiService.creditRequest(request.getRequestId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    requestList.remove(request);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Request credited successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to credit request", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRejectDialog(RequestModel request) {
        EditText input = new EditText(getContext());
        input.setHint("Enter rejection remarks");
        new AlertDialog.Builder(getContext())
                .setTitle("Reject Request")
                .setView(input)
                .setPositiveButton("Reject", (dialog, which) -> rejectRequest(request, input.getText().toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void rejectRequest(RequestModel request, String remarks) {
        Map<String, String> body = new HashMap<>();
        body.put("remarks", remarks);
        // CORRECTED: Token removed from call
        apiService.rejectRequestAdmin(request.getRequestId(), body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    requestList.remove(request);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Request rejected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to reject", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
