package com.example.reimbursementapp.api.models;

public class CreateUserRequest {

    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String role;
    private String teamLeadId;

    public CreateUserRequest(String email, String password, String fullName,
                             String phone, String role, String teamLeadId) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.teamLeadId = teamLeadId;
    }

    // --- Getters ---
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getTeamLeadId() { return teamLeadId; }

    // --- Setters ---
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void setTeamLeadId(String teamLeadId) { this.teamLeadId = teamLeadId; }
}
