package com.example.reimbursementapp;

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

public class ViewRequestsFragment extends Fragment {

    private static final String ARG_ROLE = "role";

    private String role;
    private RecyclerView recyclerViewRequests;
    private RequestAdapter requestAdapter;
    private ArrayList<RequestModel> requestList;

    public ViewRequestsFragment() { }

    public static ViewRequestsFragment newInstance(String role) {
        ViewRequestsFragment fragment = new ViewRequestsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE, role);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            role = getArguments().getString(ARG_ROLE, "staff");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_request, container, false);
        recyclerViewRequests = view.findViewById(R.id.recyclerViewRequests);
        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(requireContext()));

        requestList = new ArrayList<>();
        requestAdapter = new RequestAdapter(requestList, requireContext(), role, null);
        recyclerViewRequests.setAdapter(requestAdapter);

        loadRequests();
        return view;
    }

    private void loadRequests() {
        ApiService api = ApiClient.getAuthenticatedApiService(requireContext());

        Call<List<RequestModel>> call;
        switch (role.toLowerCase()) {
            case "teamlead":
                call = api.getPendingRequestsTL();
                break;
            case "admin":
                call = api.getPendingStaffRequests();
                break;
            default:
                call = api.getMyRequests();
                break;
        }

        call.enqueue(new Callback<List<RequestModel>>() {
            @Override
            public void onResponse(Call<List<RequestModel>> call, Response<List<RequestModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requestList.clear();
                    requestList.addAll(response.body());
                    requestAdapter.notifyDataSetChanged();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("ViewRequests", "Error response: " + errorBody);
                        Toast.makeText(requireContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RequestModel>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
