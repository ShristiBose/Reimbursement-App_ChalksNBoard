package com.example.reimbursementapp;
import com.google.gson.Gson;

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

public class AllApprovedRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private ArrayList<RequestModel> requestList = new ArrayList<>();
    private String token;
    private ApiService apiService;

    public AllApprovedRequestFragment(String token) {
        this.token = token;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // It's better practice to use a specific layout if available, but using the pending one works if they are identical.
        View view = inflater.inflate(R.layout.fragment_approved_request, container, false);
        apiService = ApiClient.getAuthenticatedApiService(requireContext());

        recyclerView = view.findViewById(R.id.recyclerViewApproved); // Ensure this ID matches your layout
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // For a view-only list, the action listener can be null.
        adapter = new RequestAdapter(requestList, getContext(), "admin", null);
        recyclerView.setAdapter(adapter);

        loadCreditedRequests();
        return view;
    }

    private void loadCreditedRequests() {
        apiService.getCreditedRequests().enqueue(new Callback<List<RequestModel>>() {
            @Override
            public void onResponse(Call<List<RequestModel>> call, Response<List<RequestModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body());
                    Log.d("DEBUG", "Raw JSON (Credited Requests): " + json);

                    requestList.clear();
                    for (RequestModel r : response.body()) {
                        // FIX: Ensure requestType is set
                        if (r.getRequestType() == null || r.getRequestType().isEmpty()) {
                            r.setRequestType(r.getTeamLeadId() != null ? "teamlead" : "staff");
                            Log.d("DEBUG", "Set requestType for billId=" + r.getBillId() + " -> " + r.getRequestType());
                        }
                        requestList.add(r);
                    }

                    adapter.notifyDataSetChanged();

                    for (RequestModel r : requestList) {
                        Log.d("DEBUG", "Credited Request loaded: billId=" + r.getBillId() + ", requestType=" + r.getRequestType());
                    }

                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("DEBUG", "Failed to load credited requests: " + response.code() + " - " + error);
                        Toast.makeText(getContext(), "Failed to load credited requests", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to load credited requests", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RequestModel>> call, Throwable t) {
                Log.e("DEBUG", "Error loading credited requests: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });

    }
}
