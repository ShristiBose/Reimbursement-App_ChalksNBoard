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

    private String token;
    private String userId;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_lead);


        token = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString("JWT_TOKEN", null);
        userId = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString("USER_ID", null);

        apiService = ApiClient.getApiService();

        BottomNavigationView bottomNav = findViewById(R.id.teamLeadBottomNav);


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
                fragment = ApprovedRequestFragment.newInstance(token);
            } else if (itemId == R.id.nav_my_requests) {

                fragment = ViewRequestsFragment.newInstance( token);
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


    public void approveRequest(String requestId) {
        apiService.approveRequestTL(requestId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TeamLeadDashboardActivity.this, "Request Approved", Toast.LENGTH_SHORT).show();

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


    public void rejectRequest(String requestId, String remarks) {
        Map<String, String> body = new HashMap<>();
        body.put("remarks", remarks);

        apiService.rejectRequestTL(requestId, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TeamLeadDashboardActivity.this, "Request Rejected", Toast.LENGTH_SHORT).show();

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
