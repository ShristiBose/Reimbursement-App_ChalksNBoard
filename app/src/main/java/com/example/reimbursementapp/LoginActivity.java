// In LoginActivity.java

package com.example.reimbursementapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reimbursementapp.api.models.LoginRequest;
import com.example.reimbursementapp.api.models.LoginResponse;
import com.example.reimbursementapp.api.models.UserModel;

import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private ApiService apiService;
    // RadioGroup is no longer needed for redirection logic but still used for selection.
    private RadioGroup radioGroupRoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        radioGroupRoles = findViewById(R.id.radioGroupRoles);

        // Use the non-authenticated service for the login call.
        apiService = ApiClient.getApiService();

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // This call now expects the new, structured LoginResponse.
        Call<LoginResponse> call = apiService.login(new LoginRequest(email, password));

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String token = loginResponse.getToken();
                    UserModel user = loginResponse.getUser();

                    // ✅ FIXED: Check if token and user data are valid.
                    if (token != null && !token.isEmpty() && user != null && user.getRole() != null) {

                        // Save all necessary data to SharedPreferences for future use.
                        SharedPreferences.Editor editor = getSharedPreferences("APP_PREFS", MODE_PRIVATE).edit();
                        editor.putString("JWT_TOKEN", token);
                        editor.putString("USER_ID", user.getId());
                        editor.putString("USER_ROLE", user.getRole());
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Login Successful as " + user.getRole(), Toast.LENGTH_SHORT).show();

                        // ✅ FIXED: Redirect based on the role received from the server (more secure).
                        redirectToDashboard(user.getRole());

                    } else {
                        Toast.makeText(LoginActivity.this, "Login successful, but response data is incomplete.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    String err = "Login failed. Please check your credentials.";
                    try {
                        if (response.errorBody() != null) {
                            err = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e("LoginActivity", "Error reading error body", e);
                    }
                    Toast.makeText(LoginActivity.this, err, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // This callback handles network failures, not JSON parsing errors.
                Log.e("LoginActivity", "Network call failed", t);
                Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ ADDED: A dedicated method for redirection.
    private void redirectToDashboard(String role) {
        Intent intent;
        switch (role.toUpperCase()) {
            case "ADMIN":
                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                break;
            case "TEAMLEAD": // Or "TEAM_LEAD" depending on your backend
            case "TEAM_LEAD":
                intent = new Intent(LoginActivity.this, TeamLeadDashboardActivity.class);
                break;
            case "STAFF":
            default:
                intent = new Intent(LoginActivity.this, StaffDashboardActivity.class);
                break;
        }
        startActivity(intent);
        finish(); // Close the login activity.
    }
}
