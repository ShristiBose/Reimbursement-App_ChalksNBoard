package com.example.reimbursementapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        jwtToken = getIntent().getStringExtra("jwtToken");

        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNav);

        // Load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    // CORRECTED: Pass the token to the default fragment
                    .replace(R.id.adminFragmentContainer, new AddUserFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_add_user) {
                // CORRECTED: Pass the token when this item is selected
                selectedFragment = new AddUserFragment();
            } else if (itemId == R.id.nav_pending_staff_requests) {
                selectedFragment = new PendingStaffRequestFragment(jwtToken);
            } else if (itemId == R.id.nav_pending_teamlead_requests) {
                selectedFragment = new PendingTeamLeadRequestFragment(jwtToken);
            } else if (itemId == R.id.nav_all_approved_requests) {
                selectedFragment = new AllApprovedRequestFragment(jwtToken);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.adminFragmentContainer, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}
