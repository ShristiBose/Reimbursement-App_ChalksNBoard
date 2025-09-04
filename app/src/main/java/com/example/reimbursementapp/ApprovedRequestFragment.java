package com.example.reimbursementapp;
import android.content.SharedPreferences;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reimbursementapp.api.models.RequestModel;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApprovedRequestFragment extends Fragment {

    private static final String TAG = "ApprovedRequestFragment";
    private static final String ARG_TOKEN = "jwtToken";

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private ArrayList<RequestModel> approvedList;
    private String jwtToken;

    public ApprovedRequestFragment() {}

    public static ApprovedRequestFragment newInstance(String jwtToken) {
        ApprovedRequestFragment fragment = new ApprovedRequestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOKEN, jwtToken);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jwtToken = getArguments().getString(ARG_TOKEN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_approved_request, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewApproved);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        approvedList = new ArrayList<>();
        adapter = new RequestAdapter(approvedList, requireContext(), "teamlead", null);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadApprovedStaffRequests();
    }

    private void loadApprovedStaffRequests() {
        Log.d(TAG, "loadApprovedStaffRequests() CALLED");


        SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String tlUserId = prefs.getString("USER_ID", null);
        String jwtToken = prefs.getString("JWT_TOKEN", null);

        Log.d(TAG, "TL User ID from prefs: " + tlUserId);
        Log.d(TAG, "JWT Token present: " + (jwtToken != null));
        Log.d(TAG, "JWT Token length: " + (jwtToken != null ? jwtToken.length() : 0));

        if (tlUserId == null) {
            Toast.makeText(getContext(), "Error: TL user ID not found in preferences", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getAuthenticatedApiService(requireContext());


        Log.d(TAG, "Making API call to: /api/tl/bills/approved");

        api.getApprovedRequestsTL().enqueue(new Callback<List<RequestModel>>() {
            @Override
            public void onResponse(Call<List<RequestModel>> call, Response<List<RequestModel>> response) {
                Log.d(TAG, "API Response received. Code: " + response.code());

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<RequestModel> allApproved = response.body();
                        Log.d(TAG, "API returned " + allApproved.size() + " requests");


                        for (int i = 0; i < allApproved.size(); i++) {
                            RequestModel request = allApproved.get(i);
                            Log.d(TAG, "Request[" + i + "]: " +
                                    "billId=" + request.getBillId() +
                                    ", status=" + request.getStatus() +
                                    ", staffId=" + request.getStaffId() +
                                    ", teamLeadId=" + request.getTeamLeadId() +
                                    ", staffName=" + request.getStaffName() +
                                    ", isTLRequest=" + (tlUserId.equals(request.getStaffId())));
                        }


                        List<RequestModel> staffRequestsOnly = new ArrayList<>();
                        for (RequestModel request : allApproved) {
                            if (!tlUserId.equals(request.getStaffId())) {
                                staffRequestsOnly.add(request);
                            }
                        }

                        approvedList.clear();
                        approvedList.addAll(staffRequestsOnly);
                        adapter.notifyDataSetChanged();

                        Log.d(TAG, "After filtering - Staff requests: " + staffRequestsOnly.size() +
                                ", TL own requests: " + (allApproved.size() - staffRequestsOnly.size()));

                        if (approvedList.isEmpty()) {
                            if (allApproved.isEmpty()) {
                                Toast.makeText(getContext(), "API returned 0 approved requests", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(),
                                        "Found " + allApproved.size() + " requests but all are TL's own requests",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getContext(),
                                    "Showing " + approvedList.size() + " staff requests approved by you",
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.e(TAG, "Response body is null");
                        Toast.makeText(requireContext(), "Server returned empty response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Detailed error logging
                    Log.e(TAG, "API Error - Code: " + response.code() + ", Message: " + response.message());

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error Body: " + errorBody);
                            Toast.makeText(requireContext(),
                                    "Server error: " + response.code() + " - " + errorBody,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Server error: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                        Toast.makeText(requireContext(),
                                "Error: " + response.code() + " - " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RequestModel>> call, Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage(), t);
                Toast.makeText(requireContext(),
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();


                if (t instanceof javax.net.ssl.SSLHandshakeException) {
                    Log.e(TAG, "SSL Handshake error - check ngrok certificate");
                }
            }
        });
    }
}
