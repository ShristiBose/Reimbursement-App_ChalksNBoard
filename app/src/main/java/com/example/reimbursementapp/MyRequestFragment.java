package com.example.reimbursementapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class MyRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private List<RequestModel> requestList;
    private FirebaseFirestore db;
    private String staffId;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_request, container, false);

        recyclerView = view.findViewById(R.id.rvMyRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        requestList = new ArrayList<>();
        adapter = new RequestAdapter(requestList, getActivity(), false);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        staffId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadRequests();

        return view;
    }

    private void loadRequests() {
        Query query = db.collection("requests")
                .whereEqualTo("staffId", staffId);

        listenerRegistration = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error loading requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            requestList.clear();
            for (QueryDocumentSnapshot doc : value) {
                RequestModel request = doc.toObject(RequestModel.class);
                request.setRequestId(doc.getId()); // Set the document ID
                requestList.add(request);
            }
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}