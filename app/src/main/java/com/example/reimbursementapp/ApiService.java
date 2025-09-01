package com.example.reimbursementapp;

import com.example.reimbursementapp.api.models.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    //  Login
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    //  Admin: Create user
    @POST("/api/admin/users")
    Call<Void> createUser(@Body CreateUserRequest request);

    //  Team Leads list
    @GET("/api/teamleads")
    Call<List<UserModel>> getTeamLeads();

    //   Create reimbursement request
    @POST("/api/bills")
    Call<CreateRequestResponse> createRequest(@Body CreateRequestRequest request);

    //  View own requests (staff)
    @GET("/api/bills/my-requests")
    Call<List<RequestModel>> getMyRequests();


    // TEAM LEAD
    @GET("/api/tl/me/bills")
    Call<List<RequestModel>> getMyBillsTL();

    @GET("/api/tl/bills/approved")
    Call<List<RequestModel>> getApprovedRequestsTL();

    @GET("/api/tl/bills/pending")
    Call<List<RequestModel>> getPendingRequestsTL();

    @GET("/api/tl/bills/pending-admin")
    Call<List<RequestModel>> getPendingAdminRequestsTL();

    @GET("/api/tl/bills/{billId}")
    Call<RequestModel> getBillDetailsTL(@Path("billId") String billId);

    @POST("/api/tl/bills/{billId}/approve")
    Call<Void> approveRequestTL(@Path("billId") String billId);

    @POST("/api/tl/bills/{billId}/reject")
    Call<Void> rejectRequestTL(@Path("billId") String billId, @Body Map<String, String> body);


    // ADMIN
    @GET("/api/admin/users")
    Call<List<UserModel>> getAllUsers();

    @GET("/api/admin/bills/staff-pending")
    Call<List<RequestModel>> getPendingStaffRequests();

    @GET("/api/admin/bills/teamlead-pending")
    Call<List<RequestModel>> getPendingTeamLeadRequests();

    @GET("/api/admin/bills/approved")
    Call<List<RequestModel>> getCreditedRequests();

    @GET("/api/admin/bills/pending")
    Call<List<RequestModel>> getPendingAdminRequests();

    // ROLE-SPECIFIC ADMIN ACTIONS (legacy)
    @POST("/api/admin/bills/staff/{billId}/approve")
    Call<Void> approveStaffBill(@Path("billId") String billId);

    @POST("/api/admin/bills/teamlead/{billId}/approve")
    Call<Void> approveTeamLeadBill(@Path("billId") String billId);

    @POST("/api/admin/bills/staff/{billId}/reject")
    Call<Void> rejectStaffBill(@Path("billId") String billId, @Body Map<String, String> body);

    @POST("/api/admin/bills/teamlead/{billId}/reject")
    Call<Void> rejectTeamLeadBill(@Path("billId") String billId, @Body Map<String, String> body);

    @POST("/api/admin/bills/staff/{billId}/credit")
    Call<Void> creditStaffBill(@Path("billId") String billId);

    @POST("/api/admin/bills/teamlead/{billId}/credit")
    Call<Void> creditTeamLeadBill(@Path("billId") String billId);


    // GENERIC ADMIN ACTIONS (recommended)
    @POST("/api/admin/bills/{billId}/credit")
    Call<Void> creditBill(@Path("billId") String billId);

    @POST("/api/admin/bills/{billId}/reject")
    Call<Void> rejectBill(@Path("billId") String billId, @Body Map<String, String> body);


    // RECEIPT ENDPOINTS (Admin)
    @GET("/api/admin/bills/staff/{billId}/receipt")
    Call<ReceiptModel> getStaffReceipt(@Path("billId") String billId);

    @GET("/api/admin/bills/teamlead/{billId}/receipt")
    Call<ReceiptModel> getTeamLeadReceiptAdmin(@Path("billId") String billId);

    //  ENDPOINTS FOR VIEWING RECEIPTS
    @GET("api/bills/{billId}/receipt")
    Call<ReceiptModel> getStaffReceiptUser(@Path("billId") String billId);

    @GET("api/tl/bills/{billId}/receipt")
    Call<ReceiptModel> getTeamLeadReceiptUser(@Path("billId") String billId);
}
