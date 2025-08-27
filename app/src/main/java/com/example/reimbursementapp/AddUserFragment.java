package com.example.reimbursementapp;

import android.content.Context;
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

    public AddUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getAuthenticatedApiService(requireContext());
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

        Context safeContext = requireContext();
        String[] roles = {"Admin", "TEAM_LEAD", "STAFF"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(safeContext, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        teamLeadAdapter = new ArrayAdapter<>(safeContext, android.R.layout.simple_spinner_item, teamLeadNames);
        teamLeadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeamLead.setAdapter(teamLeadAdapter);

        loadTeamLeads();
        setupListeners();
        return view;
    }

    private void setupListeners() {
        spinnerRole.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String role = spinnerRole.getSelectedItem().toString();
                if (role.equals("STAFF")) {
                    spinnerTeamLead.setVisibility(View.VISIBLE);
                } else {
                    spinnerTeamLead.setVisibility(View.GONE);
                    selectedTeamLeadId = null;
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                spinnerTeamLead.setVisibility(View.GONE);
            }
        });

        spinnerTeamLead.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedTeamLeadId = null;
                } else {
                    selectedTeamLeadId = teamLeadIds.get(position - 1);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedTeamLeadId = null;
            }
        });

        btnAddUser.setOnClickListener(v -> addUser());
    }

    private void loadTeamLeads() {
        spinnerTeamLead.setEnabled(false);
        spinnerTeamLead.setVisibility(View.GONE);

        apiService.getTeamLeads().enqueue(new Callback<List<UserModel>>() {
            @Override
            public void onResponse(Call<List<UserModel>> call, Response<List<UserModel>> response) {
                if (!isAdded()) return;

                teamLeadNames.clear();
                teamLeadIds.clear();
                teamLeadNames.add("Select a Team Lead");

                if (response.isSuccessful() && response.body() != null) {
                    for (UserModel u : response.body()) {
                        if (u.getId() != null) {
                            teamLeadIds.add(u.getId());
                            // ✅ FIXED: Changed u.getName() to u.getFullName()
                            String displayName = u.getFullName() != null ? u.getFullName() : u.getEmail();
                            teamLeadNames.add(displayName);
                        }
                    }
                    teamLeadAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load team leads.", Toast.LENGTH_SHORT).show();
                }
                spinnerTeamLead.setEnabled(true);
            }

            @Override
            public void onFailure(Call<List<UserModel>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network error loading team leads.", Toast.LENGTH_SHORT).show();
                }
                spinnerTeamLead.setEnabled(true);
            }
        });
    }

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

        if (role.equals("STAFF") && selectedTeamLeadId == null) {
            Toast.makeText(getContext(), "Please select a Team Lead for the staff member", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateUserRequest request = new CreateUserRequest(email, password, fullName, phone, role,
                role.equals("STAFF") ? selectedTeamLeadId : null);

        apiService.createUser(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "User added successfully!", Toast.LENGTH_SHORT).show();
                    resetFields();
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Failed to add user.";
                        Toast.makeText(getContext(), "Error: " + err, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                        Toast.makeText(getContext(), "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
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
