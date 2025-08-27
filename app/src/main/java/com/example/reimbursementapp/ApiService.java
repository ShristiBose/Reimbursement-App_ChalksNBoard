package com.example.reimbursementapp;

import com.example.reimbursementapp.api.models.*;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("/api/auth/login")
    Call<ResponseBody> login(@Body LoginRequest loginRequest);

    @POST("/api/admin/users")
    Call<CreateUserResponse> createUser(@Body CreateUserRequest request);

    // CORRECTED endpoint for team leads
    @GET("/api/teamleads")
    Call<List<UserModel>> getTeamLeads();

    @POST("/api/requests")
    Call<CreateRequestResponse> createRequest(@Body CreateRequestRequest request);

    @GET("/api/me/requests")
    Call<List<RequestModel>> getMyRequests();

    @GET("/api/tl/requests/pending")
    Call<List<RequestModel>> getPendingRequestsTL();

    @POST("/api/tl/requests/{requestId}/approve")
    Call<Void> approveRequestTL(@Path("requestId") String requestId);

    @POST("/api/tl/requests/{requestId}/reject")
    Call<Void> rejectRequestTL(@Path("requestId") String requestId, @Body Map<String, String> body);

    @GET("/api/tl/requests/approved")
    Call<List<RequestModel>> getApprovedRequestsTL();

    @GET("/api/admin/requests/pending-staff")
    Call<List<RequestModel>> getPendingStaffRequests();

    @GET("/api/admin/requests/pending-teamlead")
    Call<List<RequestModel>> getPendingTeamLeadRequests();

    @POST("/api/admin/requests/{requestId}/credit")
    Call<Void> creditRequest(@Path("requestId") String requestId);

    @POST("/api/admin/requests/{requestId}/reject")
    Call<Void> rejectRequestAdmin(@Path("requestId") String requestId, @Body Map<String, String> body);

    @GET("/api/admin/requests/credited")
    Call<List<RequestModel>> getCreditedRequests();

    @GET("/api/receipts/{receiptId}")
    Call<ReceiptModel> getReceiptById(@Path("receiptId") String receiptId);

    @GET("/api/me/receipts")
    Call<List<ReceiptModel>> getMyReceipts();
}
