package com.example.reimbursementapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.reimbursementapp.api.models.CreateUserRequest;
import com.example.reimbursementapp.api.models.CreateUserResponse;
import com.example.reimbursementapp.api.models.UserModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddUserFragment extends Fragment {

    private static final String TAG = "AddUserFragment";

    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Spinner spinnerRole, spinnerTeamLead;
    private Button btnAddUser;
    private ApiService apiService;

    private ArrayList<String> teamLeadNames = new ArrayList<>();
    private ArrayList<String> teamLeadIds = new ArrayList<>();
    private ArrayAdapter<String> teamLeadAdapter;
    private String selectedTeamLeadId = null;

    private String jwtToken;

    // Constructor
    public AddUserFragment(String token) {
        this.jwtToken = token;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_user, container, false);

        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        spinnerRole = view.findViewById(R.id.spinnerRole);
        spinnerTeamLead = view.findViewById(R.id.spinnerTeamLead);
        btnAddUser = view.findViewById(R.id.btnAddUser);

        // DEBUG: JWT token
        Log.d(TAG, "JWT Token: " + jwtToken);

        // Initialize ApiService
        apiService = ApiClient.getApiService(jwtToken);

        // Role spinner
        String[] roles = {"Admin", "TeamLead", "Staff"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // TeamLead spinner adapter
        teamLeadAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, teamLeadNames);
        teamLeadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeamLead.setAdapter(teamLeadAdapter);

        // Role selection listener
        spinnerRole.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String role = spinnerRole.getSelectedItem().toString();
                if (role.equals("Staff")) {
                    spinnerTeamLead.setVisibility(View.VISIBLE);
                    loadTeamLeads(); // call API
                } else {
                    spinnerTeamLead.setVisibility(View.GONE);
                    selectedTeamLeadId = null;
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnAddUser.setOnClickListener(v -> addUser());

        return view;
    }

    // -----------------------------
    // Load team leads from API
    // -----------------------------
    private void loadTeamLeads() {
        Log.d(TAG, "Calling getTeamLeads API...");

        apiService.getTeamLeads().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserModel> leads = response.body();
                    Log.d(TAG, "Raw team leads: " + leads);

                    teamLeadNames.clear();
                    teamLeadIds.clear();

                    if (leads.isEmpty()) {
                        Toast.makeText(getContext(), "No Team Leads found", Toast.LENGTH_SHORT).show();
                    }

                    teamLeadNames.add("Select a Team Lead");
                    teamLeadIds.add(null);

                    for (UserModel u : leads) {
                        Log.d(TAG, "TeamLead: " + u.getId() + " | " + u.getName());
                        teamLeadIds.add(u.getId());
                        teamLeadNames.add(u.getName());
                    }

                    teamLeadAdapter.notifyDataSetChanged();

                    spinnerTeamLead.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                            selectedTeamLeadId = (position > 0) ? teamLeadIds.get(position) : null;
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {
                            selectedTeamLeadId = null;
                        }
                    });

                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error loading team leads: " + err);
                        Toast.makeText(getContext(), "Error: " + err, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                Log.e(TAG, "API call failed: ", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // -----------------------------
    // Add user API
    // -----------------------------
    private void addUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (role.equals("Staff") && selectedTeamLeadId == null) {
            Toast.makeText(getContext(), "Please select a Team Lead", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateUserRequest request = new CreateUserRequest(email, password, fullName, phone, role,
                role.equals("Staff") ? selectedTeamLeadId : null);

        apiService.createUser(request).enqueue(new Callback<CreateUserResponse>() {
            @Override
            public void onResponse(Call<CreateUserResponse> call, Response<CreateUserResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "User added successfully", Toast.LENGTH_SHORT).show();
                    resetFields();
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(getContext(), "Failed to add user: " + err, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<CreateUserResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetFields() {
        etFullName.setText("");
        etEmail.setText("");
        etPhone.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
        spinnerRole.setSelection(0);
        spinnerTeamLead.setSelection(0);
        spinnerTeamLead.setVisibility(View.GONE);
        selectedTeamLeadId = null;
    }
}
