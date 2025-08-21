package com.example.reimbursementapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class ViewRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRequests;
    private RequestAdapter requestAdapter;
    private List<RequestModel> requestList;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);

        recyclerViewRequests = findViewById(R.id.recyclerViewRequests);
        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        requestAdapter = new RequestAdapter(requestList, this, false);
        recyclerViewRequests.setAdapter(requestAdapter);

        db = FirebaseFirestore.getInstance();

        // Get current user safely
        String staffId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (staffId != null) {
            loadRequests(staffId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if user is not logged in
        }
    }

    private void loadRequests(String staffId) {
        Query query = db.collection("requests")
                .whereEqualTo("staffId", staffId);

        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ViewRequestsActivity.this, "Error loading requests", Toast.LENGTH_SHORT).show();
                    return;
                }

                requestList.clear();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        RequestModel request = doc.toObject(RequestModel.class);
                        request.setRequestId(doc.getId()); // Set the document ID
                        requestList.add(request);
                    }
                    requestAdapter.notifyDataSetChanged();

                    if (requestList.isEmpty()) {
                        Toast.makeText(ViewRequestsActivity.this, "No requests found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
