package com.example.reimbursementapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPhone, edtPassword, edtConfirmPassword;
    private Spinner spinnerRole;
    private Button btnAddUser;
    private ProgressDialog progressDialog;

    private FirebaseAuth adminAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        adminAuth = FirebaseAuth.getInstance();  // Admin stays logged in
        usersRef = FirebaseDatabase.getInstance().getReference("Users");  // Make sure this matches your DB

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnAddUser = findViewById(R.id.btnAddUser);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Setup Role Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Staff", "TeamLead"});  // Removed HR
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnAddUser.setOnClickListener(v -> addUser());
    }

    private void addUser() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Adding user...");
        progressDialog.show();

        // Use secondary auth instance to create user without logging out admin
        FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(FirebaseDatabase.getInstance().getApp());
        secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser newUser = authResult.getUser();
                    if (newUser != null) {
                        String uid = newUser.getUid();

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("fullName", fullName);
                        userMap.put("email", email);
                        userMap.put("phone", phone);
                        userMap.put("role", role);

                        usersRef.child(uid).setValue(userMap)
                                .addOnSuccessListener(unused -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show();
                                    clearFields();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to create user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearFields() {
        edtFullName.setText("");
        edtEmail.setText("");
        edtPhone.setText("");
        edtPassword.setText("");
        edtConfirmPassword.setText("");
        spinnerRole.setSelection(0);
    }
}
