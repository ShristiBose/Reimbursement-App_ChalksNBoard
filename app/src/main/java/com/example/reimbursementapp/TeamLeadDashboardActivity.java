package com.example.reimbursementapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamLeadDashboardActivity extends AppCompatActivity {

    private String token; // JWT token after login
    private String userId; // TeamLead ID
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_lead);

        // Retrieve token from SharedPreferences
        token = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", null);
        userId = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString("USER_ID", null); // Assuming USER_ID is saved on login

        apiService = ApiClient.getApiService();

        BottomNavigationView bottomNav = findViewById(R.id.teamLeadBottomNav);

        // Default fragment on launch
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.teamLeadFragmentContainer, new NewRequestFragment(token, "TeamLead"))
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_new_request) {
                fragment = new NewRequestFragment(token, "TeamLead");
            } else if (itemId == R.id.nav_pending) {
                fragment = new PendingRequestFragment(token);
            } else if (itemId == R.id.nav_approved) {
                fragment = new ApprovedRequestFragment(token);
            } else if (itemId == R.id.nav_my_requests) {
                // Pass the user's role and token to the generic "View Requests" fragment
                fragment = ViewRequestsFragment.newInstance("TeamLead", token);
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.teamLeadFragmentContainer, fragment)
                        .commit();
            }
            return true;
        });
    }

    // This method is now redundant as its logic is handled within PendingRequestFragment.
    // However, if called from another context, this corrected version will work.
    public void approveRequest(String requestId) {
        // CORRECTED: Token removed from call, added <Void> type to Callback
        apiService.approveRequestTL(requestId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TeamLeadDashboardActivity.this, "Request Approved", Toast.LENGTH_SHORT).show();
                    // Consider adding a refresh mechanism here if needed
                } else {
                    Toast.makeText(TeamLeadDashboardActivity.this, "Failed to approve", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(TeamLeadDashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // This method is also redundant but corrected for completeness.
    public void rejectRequest(String requestId, String remarks) {
        Map<String, String> body = new HashMap<>();
        body.put("remarks", remarks);
        // CORRECTED: Token removed from call, added <Void> type to Callback
        apiService.rejectRequestTL(requestId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TeamLeadDashboardActivity.this, "Request Rejected", Toast.LENGTH_SHORT).show();
                    // Consider adding a refresh mechanism here if needed
                } else {
                    Toast.makeText(TeamLeadDashboardActivity.this, "Failed to reject", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(TeamLeadDashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
