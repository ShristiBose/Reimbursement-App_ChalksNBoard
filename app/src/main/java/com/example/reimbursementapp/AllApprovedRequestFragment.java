package com.example.reimbursementapp;

import android.os.Bundle;
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
        // The ApiService instance is created with the token,
        // which allows the interceptor to add it to headers.
        this.apiService = ApiClient.getApiService(token);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // It's better practice to use a specific layout if available, but using the pending one works if they are identical.
        View view = inflater.inflate(R.layout.fragment_approved_request, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewApproved); // Ensure this ID matches your layout
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // For a view-only list, the action listener can be null.
        adapter = new RequestAdapter(requestList, getContext(), "admin", null);
        recyclerView.setAdapter(adapter);

        loadCreditedRequests();
        return view;
    }

    private void loadCreditedRequests() {
        // CORRECTED: The "Bearer " + token parameter is removed from the call.
        // The ApiClient's interceptor handles adding the Authorization header.
        apiService.getCreditedRequests().enqueue(new Callback<List<RequestModel>>() {
            @Override
            public void onResponse(Call<List<RequestModel>> call, Response<List<RequestModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requestList.clear();
                    requestList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load credited requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RequestModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
