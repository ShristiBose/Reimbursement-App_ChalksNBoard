package com.example.reimbursementapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaffDashboardActivity extends AppCompatActivity {

    private static final int PICK_BILL_REQUEST = 1001;

    private Spinner spinnerCategory;
    private EditText edtAmount, edtDescription;
    private Button btnUploadBill, btnSubmitRequest;
    private TextView tvWelcomeStaff, tvSelectedFileName;
    private ImageView ivBillPreview;

    private Uri billFileUri; // Selected bill file

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private ProgressDialog progressDialog;

    // ✅ Category → Max allowed amount
    private final Map<String, Double> categoryLimits = new HashMap<String, Double>() {{
        put("Travel", 5000.0);
        put("Accommodation", 3000.0);
        put("Food", 1000.0);
        put("Office Supplies", 2000.0);
        put("Other", 500.0);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Init UI
        tvWelcomeStaff = findViewById(R.id.tvWelcomeStaff);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        edtAmount = findViewById(R.id.edtAmount);
        edtDescription = findViewById(R.id.edtDescription);
        btnUploadBill = findViewById(R.id.btnUploadBill);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
        ivBillPreview = findViewById(R.id.ivBillPreview);

        // Show welcome message
        String staffName = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "Staff";
        tvWelcomeStaff.setText("Welcome, " + staffName);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // Setup Spinner values
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Travel", "Accommodation", "Food", "Office Supplies", "Other"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Upload bill
        btnUploadBill.setOnClickListener(v -> openFileChooser());

        // Submit request
        btnSubmitRequest.setOnClickListener(v -> submitRequest());

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.staffBottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_new_request) {
                // Show the request form (current layout)
                findViewById(R.id.requestFormContainer).setVisibility(View.VISIBLE);
                findViewById(R.id.staffFragmentContainer).setVisibility(View.GONE);
            } else if (itemId == R.id.nav_view_requests) {
                // Hide the form and show the fragment
                findViewById(R.id.requestFormContainer).setVisibility(View.GONE);
                findViewById(R.id.staffFragmentContainer).setVisibility(View.VISIBLE);

                // Load the ViewRequestsFragment
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.staffFragmentContainer, new StaffViewRequests());
                transaction.commit();
            }
            return true;
        });

        // Set default selection
        bottomNav.setSelectedItemId(R.id.nav_new_request);
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "application/pdf"});
        startActivityForResult(Intent.createChooser(intent, "Select Bill"), PICK_BILL_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_BILL_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            billFileUri = data.getData();

            // Show the selected file name
            String fileName = getFileNameFromUri(billFileUri);
            tvSelectedFileName.setText("Selected file: " + fileName);
            tvSelectedFileName.setVisibility(View.VISIBLE);

            // Show preview for images
            if (getContentResolver().getType(billFileUri) != null &&
                    getContentResolver().getType(billFileUri).startsWith("image/")) {
                ivBillPreview.setImageURI(billFileUri);
                ivBillPreview.setVisibility(View.VISIBLE);
            } else {
                ivBillPreview.setVisibility(View.GONE);
            }

            Toast.makeText(this, "Bill selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void submitRequest() {
        String category = spinnerCategory.getSelectedItem().toString();
        String amountStr = edtAmount.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        if (amountStr.isEmpty() || description.isEmpty() || billFileUri == null) {
            Toast.makeText(this, "Please fill all fields and upload a bill", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount entered", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Check max allowed limit for category
        Double maxLimit = categoryLimits.get(category);
        if (maxLimit == null) {
            maxLimit = 0.0;
        }
        if (amount > maxLimit) {
            Toast.makeText(this, category + " limit is ₹" + maxLimit, Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Submitting request...");
        progressDialog.show();

        // Upload bill to Firebase Storage
        String fileName = "bills/" + UUID.randomUUID().toString();
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(billFileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveRequestToFirestore(category, amount, description, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to upload bill: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveRequestToFirestore(String category, double amount, String description, String billUrl) {
        String staffId = auth.getCurrentUser().getUid();
        String staffName = auth.getCurrentUser().getEmail();

        Map<String, Object> request = new HashMap<>();
        request.put("staffId", staffId);
        request.put("staffName", staffName);
        request.put("category", category);
        request.put("amount", amount);
        request.put("description", description);
        request.put("billUrl", billUrl);
        request.put("status", "Pending");
        request.put("remarks", ""); // Initialize remarks as empty

        db.collection("requests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    // Set the request ID for future reference
                    documentReference.update("requestId", documentReference.getId());

                    progressDialog.dismiss();
                    Toast.makeText(this, "Request submitted!", Toast.LENGTH_SHORT).show();

                    // Reset form
                    edtAmount.setText("");
                    edtDescription.setText("");
                    billFileUri = null;
                    tvSelectedFileName.setVisibility(View.GONE);
                    ivBillPreview.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to submit request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}