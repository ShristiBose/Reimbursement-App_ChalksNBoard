package com.example.reimbursementapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TeamLeadDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_lead);

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.teamLeadBottomNav);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_pending) {
                selectedFragment = new PendingRequestFragment();
            } else if (itemId == R.id.nav_approved) {
                selectedFragment = new ApprovedRequestFragment();
            } else if (itemId == R.id.nav_my_requests) {
                selectedFragment = new MyRequestFragment();
            }

            if (selectedFragment != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.teamLeadFragmentContainer, selectedFragment);
                transaction.commit();
            }
            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_pending);
        }
    }

    public void updateRequestStatus(RequestModel request, String status) {
        // For rejections, show a dialog to enter remarks
        if ("Rejected".equals(status)) {
            showRemarksDialog(request, status);
        } else {
            // For approvals, just update the status
            updateRequestInFirestore(request, status, "");
        }
    }

    private void showRemarksDialog(RequestModel request, String status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Remarks");

        final EditText input = new EditText(this);
        input.setHint("Reason for rejection");
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String remarks = input.getText().toString().trim();
                if (remarks.isEmpty()) {
                    Toast.makeText(TeamLeadDashboardActivity.this, "Remarks cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateRequestInFirestore(request, status, remarks);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void updateRequestInFirestore(RequestModel request, String status, String remarks) {
        db.collection("requests").document(request.getRequestId())
                .update("status", status, "remarks", remarks)
                .addOnSuccessListener(aVoid -> {
                    String message = "Request " + status.toLowerCase() + " successfully";
                    Toast.makeText(TeamLeadDashboardActivity.this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TeamLeadDashboardActivity.this, "Error updating request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}