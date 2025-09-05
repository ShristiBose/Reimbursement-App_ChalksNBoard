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
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingTeamLeadRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private ArrayList<RequestModel> requestList = new ArrayList<>();
    private String token;
    private ApiService apiService;

    private RequestModel currentRequest; // currently paying request

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
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DEBUG", "Reject Team Lead request error: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Reject failed at " + endpoint + "\nError: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ---------------- Payment integration ----------------
    private void creditRequest(RequestModel request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            Toast.makeText(getContext(), "Invalid amount for payment", Toast.LENGTH_SHORT).show();
            return;
        }
        currentRequest = request;
        startRazorpayPayment(request);
    }

    private void startRazorpayPayment(RequestModel request) {
        try {
            final Activity activity = getActivity();
            if (activity == null || activity.isFinishing() || !isAdded()) {
                Toast.makeText(getContext(), "Cannot start payment now", Toast.LENGTH_SHORT).show();
                return;
            }

            Checkout checkout = new Checkout();
            checkout.setKeyID("rzp_test_RDszzhlPQoE13a");

            JSONObject options = new JSONObject();
            options.put("name", "Your App Name");
            options.put("description", "Reimbursement Payment");
            options.put("currency", "INR");
            options.put("amount", (int)(request.getAmount() * 100));

            JSONObject prefill = new JSONObject();
            prefill.put("email", "user@example.com");
            prefill.put("contact", "9876543210");
            options.put("prefill", prefill);

            // Set theme explicitly
            options.put("theme", new JSONObject().put("color", "#673AB7"));

            checkout.open(activity, options);
        } catch (Exception e) {
            Log.e("RAZORPAY_ERROR", "Payment initialization failed", e);
            Toast.makeText(getContext(), "Payment initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    // Called from MainActivity when payment is successful
    public void onPaymentSuccessForward(String razorpayPaymentID) {
        Toast.makeText(getContext(), "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();

        if (currentRequest != null) {
            markRequestAsCredited(currentRequest, razorpayPaymentID);
        }
    }

    // Called from MainActivity when payment fails
    public void onPaymentErrorForward(int code, String response) {
        Toast.makeText(getContext(), "Payment failed: " + response, Toast.LENGTH_SHORT).show();
        // Optional: reset currentRequest or handle retry
        currentRequest = null;
    }

    private void markRequestAsCredited(RequestModel request, String paymentId) {
        Call<Void> call = apiService.creditTeamLeadBill(request.getBillId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Check if the Fragment is still active and attached to an Activity
                if (getActivity() == null || !isAdded()) {
                    return; // Do nothing if fragment is not valid
                }

                if (response.isSuccessful()) {
                    // Run UI updates on the main thread
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Request credited successfully!", Toast.LENGTH_SHORT).show();
                        requestList.remove(request);
                        adapter.notifyDataSetChanged();
                        currentRequest = null; // Reset the request
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Credit failed! Please check backend.", Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Credit failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
