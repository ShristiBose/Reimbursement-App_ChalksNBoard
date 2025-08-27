package com.example.reimbursementapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // ✅ IMPORT ADDED
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reimbursementapp.api.models.LoginRequest;
import java.io.IOException;
import okhttp3.ResponseBody; // ✅ IMPORT ADDED
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private RadioGroup radioGroupRoles;
    private RadioButton radioStaff, radioTeamLead, radioAdmin;
    private Button btnLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        radioGroupRoles = findViewById(R.id.radioGroupRoles);
        radioStaff = findViewById(R.id.radioStaff);
        radioTeamLead = findViewById(R.id.radioTeamLead);
        radioAdmin = findViewById(R.id.radioAdmin);
        btnLogin = findViewById(R.id.btnLogin);

        apiService = ApiClient.getApiService(null);

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedRoleId = radioGroupRoles.getCheckedRadioButtonId();
        String selectedRole = selectedRoleId == radioTeamLead.getId() ? "TeamLead"
                : selectedRoleId == radioAdmin.getId() ? "Admin"
                : "Staff";

        // ✅ CHANGED: The generic type for the Call is now ResponseBody
        Call<ResponseBody> call = apiService.login(new LoginRequest(email, password));

        // ✅ CHANGED: The generic type for the Callback is now ResponseBody
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Read the raw string from the response
                        String responseString = response.body().string();
                        String token = parseTokenFromMessage(responseString);

                        if (token != null) {
                            // Save JWT
                            getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                                    .edit().putString("JWT_TOKEN", token).apply();

                            Toast.makeText(LoginActivity.this,
                                    "Login Successful as " + selectedRole, Toast.LENGTH_SHORT).show();

                            // Redirect
                            switch (selectedRole) {
                                case "Staff":
                                    startActivity(new Intent(LoginActivity.this, StaffDashboardActivity.class));
                                    break;
                                case "TeamLead":
                                    startActivity(new Intent(LoginActivity.this, TeamLeadDashboardActivity.class));
                                    break;
                                case "Admin":
                                    // ✅ CHANGED: Pass token to AdminDashboardActivity
                                    Intent adminIntent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                    adminIntent.putExtra("jwtToken", token);
                                    startActivity(adminIntent);
                                    break;
                            }
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login successful, but token not found in response.", Toast.LENGTH_LONG).show();
                            Log.e("LoginActivity", "Raw successful response: " + responseString);
                        }
                    } catch (IOException e) {
                        Toast.makeText(LoginActivity.this, "Error reading response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String err = "Login failed";
                    try {
                        if (response.errorBody() != null) {
                            err = response.errorBody().string();
                            Log.e("LoginActivity", "Raw error response: " + err);
                        }
                    } catch (IOException ignored) {}
                    Toast.makeText(LoginActivity.this, err, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String parseTokenFromMessage(String message) {
        if (message == null) return null;
        String marker = "Token is: ";
        int idx = message.indexOf(marker);
        if (idx >= 0) {
            return message.substring(idx + marker.length()).trim();
        }
        return null;
    }
}
