package com.example.reimbursementapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private RadioGroup radioGroupRoles;
    private RadioButton radioStaff, radioTeamLead, radioAdmin;
    private Button btnLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

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

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedRoleId = radioGroupRoles.getCheckedRadioButtonId();
        final String selectedRole;

        if (selectedRoleId == radioTeamLead.getId()) {
            selectedRole = "TeamLead";
        } else if (selectedRoleId == radioAdmin.getId()) {
            selectedRole = "Admin";
        } else {
            selectedRole = "Staff"; // default
        }

        // Authenticate user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // ✅ Read role from Realtime Database
                            userRef.child(uid).child("role")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                String actualRole = snapshot.getValue(String.class);

                                                if (actualRole.equals(selectedRole)) {
                                                    Toast.makeText(LoginActivity.this,
                                                            "Login Successful as " + actualRole,
                                                            Toast.LENGTH_SHORT).show();

                                                    // Redirect based on actual role
                                                    switch (actualRole) {
                                                        case "Staff":
                                                            startActivity(new Intent(LoginActivity.this, StaffDashboardActivity.class));
                                                            break;
                                                        case "TeamLead":
                                                            startActivity(new Intent(LoginActivity.this, TeamLeadDashboardActivity.class));
                                                            break;
                                                        case "Admin":
                                                            startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                                                            break;
                                                    }
                                                    finish();
                                                } else {
                                                    Toast.makeText(LoginActivity.this,
                                                            "Selected role does not match your account role!",
                                                            Toast.LENGTH_LONG).show();
                                                    mAuth.signOut(); // log out
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this,
                                                        "Role not set for this user in database",
                                                        Toast.LENGTH_LONG).show();
                                                mAuth.signOut();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            Toast.makeText(LoginActivity.this,
                                                    "Database error: " + error.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
