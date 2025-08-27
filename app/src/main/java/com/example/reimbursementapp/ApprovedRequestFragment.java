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

public class ApprovedRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private ArrayList<RequestModel> approvedList;
    private String jwtToken;

    public ApprovedRequestFragment(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_approved_request, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewApproved);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        approvedList = new ArrayList<>();
        adapter = new RequestAdapter(approvedList, getContext(), "teamlead", new RequestAdapter.OnActionListener() {
            @Override public void onApprove(RequestModel request) { }
            @Override public void onReject(RequestModel request) { }
            @Override public void onCredit(RequestModel request) { }
        });
        recyclerView.setAdapter(adapter);

        loadApprovedRequests();
        return view;
    }

    private void loadApprovedRequests() {
        ApiService api = ApiClient.getApiService(jwtToken);
        api.getApprovedRequestsTL().enqueue(new Callback<List<RequestModel>>() { // CORRECTED
            @Override
            public void onResponse(Call<List<RequestModel>> call, Response<List<RequestModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    approvedList.clear();
                    approvedList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to fetch approved requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RequestModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
