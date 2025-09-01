package com.example.reimbursementapp;

import com.google.gson.Gson;
import android.content.Context;
import android.content.SharedPreferences;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
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

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_request, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewPending);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        requestList = new ArrayList<>();
        apiService = ApiClient.getAuthenticatedApiService(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String role = prefs.getString("USER_ROLE", null);
        Toast.makeText(requireContext(), "Logged-in role: " + role, Toast.LENGTH_LONG).show();


        adapter = adapter = new RequestAdapter(requestList, getContext(), "teamlead", new RequestAdapter.OnActionListener() {
            @Override
            public void onApprove(RequestModel request) {
                approveRequest(request);
            }

            @Override
            public void onReject(RequestModel request) {
                showRejectDialog(request);
            }

            @Override
            public void onCredit(RequestModel request) {
            }
        });

        recyclerView.setAdapter(adapter);
        loadPendingRequests();
        return view;
    }

    private void loadPendingRequests() {
        apiService.getPendingRequestsTL().enqueue(new Callback<List<RequestModel>>() {
            @Override
            public void onResponse(Call<List<RequestModel>> call, Response<List<RequestModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body());
                    Log.d("DEBUG", "Raw JSON: " + json);
                    requestList.clear();
                    requestList.addAll(response.body());
                    adapter.notifyDataSetChanged();


                    for (RequestModel r : response.body()) {
                        Log.d("DEBUG", "Request loaded: billId=" + r.getBillId() + ", status=" + r.getStatus());
                        Toast.makeText(requireContext(),
                                "Loaded Request - billId: " + r.getBillId(),
                                Toast.LENGTH_SHORT).show();
                    }

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



    private void approveRequest(RequestModel request) {
        String billId = request.getBillId();
        Log.d("DEBUG", "Approving request, billId=" + billId);

        if (billId == null || billId.isEmpty()) {
            Toast.makeText(getContext(), "Cannot approve: billId is null!", Toast.LENGTH_LONG).show();
            return;
        }

        apiService.approveRequestTL(billId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Request approved and forwarded to Admin", Toast.LENGTH_SHORT).show();
                    loadPendingRequests();
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(getContext(),
                                "Failed to approve: " + response.code() + " - " + error,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(),
                                "Failed to approve: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showRejectDialog(RequestModel request) {
        final EditText input = new EditText(requireContext());
        input.setHint("Enter rejection remarks");
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reject Request")
                .setView(input)
                .setPositiveButton("Reject", (dialog, which) -> {
                    String remarks = input.getText().toString().trim();
                    if (!remarks.isEmpty()) {
                        rejectRequest(request, remarks); // now passes RequestModel
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void rejectRequest(RequestModel request, String reason) {
        String billId = request.getBillId();
        Log.d("DEBUG", "Rejecting request, billId=" + billId);

        if (billId == null || billId.isEmpty()) {
            Toast.makeText(getContext(), "Cannot reject: billId is null!", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("reason", reason);

        apiService.rejectRequestTL(billId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Request rejected: " + reason, Toast.LENGTH_SHORT).show();
                    loadPendingRequests();
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(getContext(),
                                "Failed to reject: " + response.code() + " - " + error,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(),
                                "Failed to reject: " + response.code(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}