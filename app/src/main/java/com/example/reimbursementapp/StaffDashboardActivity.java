package com.example.reimbursementapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StaffDashboardActivity extends AppCompatActivity {

    private String jwtToken;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);

        Intent intent = getIntent();
        if (intent != null) {
            jwtToken = intent.getStringExtra("jwtToken");
            userRole = intent.getStringExtra("userRole");
        }

        BottomNavigationView bottomNav = findViewById(R.id.staffBottomNav);


        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.staffFragmentContainer, new NewRequestFragment(jwtToken, userRole))
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_new_request) {
                selectedFragment = new NewRequestFragment(jwtToken, userRole);
            } else if (item.getItemId() == R.id.nav_view_requests) {
                selectedFragment = ViewRequestsFragment.newInstance(jwtToken);
            } else if (item.getItemId() == R.id.nav_logout) {
                logoutUser();
                return true;

            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.staffFragmentContainer, selectedFragment)
                        .commit();
            }

            return true;
        });
    }
    private void logoutUser() {
        getSharedPreferences("APP_PREFS", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(StaffDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}