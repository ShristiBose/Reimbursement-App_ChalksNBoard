package com.example.reimbursementapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.Gson;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_request, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewPending);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        apiService = ApiClient.getAuthenticatedApiService(requireContext());

        adapter = new RequestAdapter(requestList, getContext(), "admin-teamlead", new RequestAdapter.OnActionListener() {
            @Override
            public void onApprove(RequestModel request) { }

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
        apiService.getPendingTeamLeadRequests().enqueue(new Callback<List<RequestModel>>() {
            @Override
            public void onResponse(Call<List<RequestModel>> call, Response<List<RequestModel>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String rawJson = new Gson().toJson(response.body());
                    Log.d("DEBUG", "Raw JSON (TeamLead Requests): " + rawJson);

                    requestList.clear();
                    requestList.addAll(response.body());
                    adapter.notifyDataSetChanged();


                    for (RequestModel r : response.body()) {
                        Log.d("DEBUG", "TeamLead Request loaded: billId=" + r.getBillId() + ", status=" + r.getStatus());
                    }
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

    private void rejectRequest(RequestModel request, String reason) {
        Map<String, String> body = new HashMap<>();
        body.put("reason", reason);

        String endpoint = "/api/admin/bills/teamlead/" + request.getBillId() + "/reject";
        Log.d("DEBUG", "Rejecting Team Lead Bill with billId=" + request.getBillId() + " remarks=" + reason);

        apiService.rejectTeamLeadBill(request.getBillId(), body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    requestList.remove(request);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Request rejected successfully", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.d("DEBUG", "Reject Team Lead failed. Endpoint=" + endpoint + " Code=" + response.code() + " Error=" + errorBody);
                        Toast.makeText(getContext(), "Reject failed at " + endpoint + "\nCode=" + response.code() + "\nError=" + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DEBUG", "Reject Team Lead request error: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Reject failed at " + endpoint + "\nError: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void creditRequest(RequestModel request) {
        String endpoint = "/api/admin/bills/teamlead/" + request.getBillId() + "/credit";
        Log.d("DEBUG", "Crediting Team Lead Bill with billId=" + request.getBillId());

        apiService.creditTeamLeadBill(request.getBillId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    requestList.remove(request);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Amount credited successfully", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.d("DEBUG", "Credit Team Lead failed. Endpoint=" + endpoint + " Code=" + response.code() + " Error=" + errorBody);
                        Toast.makeText(getContext(), "Credit failed at " + endpoint + "\nCode=" + response.code() + "\nError=" + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DEBUG", "Credit Team Lead request error: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Credit failed at " + endpoint + "\nError: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
