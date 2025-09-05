package com.example.reimbursementapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

public class MainActivity extends AppCompatActivity implements PaymentResultListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Razorpay SDK preload (recommended)
        Checkout.preload(getApplicationContext());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Razorpay callback success
    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Log.d("PAYMENT_CALLBACK", "onPaymentSuccess triggered with ID: " + razorpayPaymentID);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        if (fragment instanceof PendingStaffRequestFragment) {
            Log.d("PAYMENT_CALLBACK", "Forwarding to PendingStaffRequestFragment");
            ((PendingStaffRequestFragment) fragment).onPaymentSuccessForward(razorpayPaymentID);
        } else if (fragment instanceof PendingTeamLeadRequestFragment) {
            Log.d("PAYMENT_CALLBACK", "Forwarding to PendingTeamLeadRequestFragment");
            ((PendingTeamLeadRequestFragment) fragment).onPaymentSuccessForward(razorpayPaymentID);
        } else {
            Log.e("PAYMENT_CALLBACK", "Error: Could not find a valid fragment to forward payment success.");
            Toast.makeText(this, "Payment successful, but could not update the list.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        Log.e("PAYMENT_CALLBACK", "onPaymentError triggered. Code: " + code + ", Response: " + response);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        if (fragment instanceof PendingStaffRequestFragment) {
            Log.d("PAYMENT_CALLBACK", "Forwarding error to PendingStaffRequestFragment");
            ((PendingStaffRequestFragment) fragment).onPaymentErrorForward(code, response);
        } else if (fragment instanceof PendingTeamLeadRequestFragment) {
            Log.d("PAYMENT_CALLBACK", "Forwarding error to PendingTeamLeadRequestFragment");
            ((PendingTeamLeadRequestFragment) fragment).onPaymentErrorForward(code, response);
        } else {
            Log.e("PAYMENT_CALLBACK", "Error: Could not find a valid fragment to forward payment error.");
            Toast.makeText(this, "Payment failed, and could not update the UI.", Toast.LENGTH_LONG).show();
        }
    }


}
