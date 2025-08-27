package com.example.reimbursementapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.reimbursementapp.api.models.CreateRequestRequest;
import com.example.reimbursementapp.api.models.CreateRequestResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewRequestFragment extends Fragment {

    private static final int PICK_FILE_REQUEST = 101;
    private static final int STORAGE_PERMISSION_REQUEST = 102;

    private TextView tvWelcome;
    private Spinner spinnerBillType;
    private EditText etDescription, etBillAmount;
    private Button btnUploadBill, btnSubmitRequest;
    private ImageView imgPreview;

    private Uri fileUri;
    private String fileBase64 = null;
    private String fileMimeType = null;
    private ProgressDialog progressDialog;

    private String userRole;
    private String jwtToken;

    private final String[] billCategories = {
            "Travel & Transportation", "Accommodation / Stay", "Food & Meals",
            "Medical Reimbursements", "Office Supplies & Utilities", "Training & Development",
            "Client / Business Entertainment", "Miscellaneous"
    };

    public NewRequestFragment(String jwtToken, String userRole) {
        this.jwtToken = jwtToken;
        this.userRole = userRole;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_request, container, false);

        tvWelcome = view.findViewById(R.id.tvWelcomeStaff);
        spinnerBillType = view.findViewById(R.id.spinnerBillType);
        etDescription = view.findViewById(R.id.etDescription);
        etBillAmount = view.findViewById(R.id.etBillAmount);
        btnUploadBill = view.findViewById(R.id.btnUploadBill);
        btnSubmitRequest = view.findViewById(R.id.btnSubmitRequest);
        imgPreview = view.findViewById(R.id.imgPreview);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, billCategories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBillType.setAdapter(adapter);

        tvWelcome.setText("Welcome, " + userRole + "!");

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);

        btnUploadBill.setOnClickListener(v -> checkStoragePermission());

        btnSubmitRequest.setOnClickListener(v -> {
            if (fileBase64 == null) {
                Toast.makeText(getContext(), "Please upload a bill first", Toast.LENGTH_SHORT).show();
            } else {
                submitRequest();
            }
        });

        return view;
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
        } else {
            chooseFile();
        }
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                fileMimeType = getContext().getContentResolver().getType(fileUri);
                fileBase64 = encodeFileToBase64(fileUri);
                imgPreview.setImageURI(fileUri);
                imgPreview.setVisibility(View.VISIBLE);
            }
        }
    }

    private String encodeFileToBase64(Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        } catch (IOException e) {
            Toast.makeText(getContext(), "File encoding failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void submitRequest() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etBillAmount.getText().toString().trim();

        if (description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedCategory = spinnerBillType.getSelectedItem().toString();

        progressDialog.setMessage("Submitting request...");
        progressDialog.show();

        CreateRequestRequest request = new CreateRequestRequest(
                amount,
                selectedCategory,
                description,
                fileBase64,
                fileMimeType
        );

        ApiClient.getApiService(jwtToken)
                .createRequest(request) // CORRECTED: Removed the token parameter from the call
                .enqueue(new Callback<CreateRequestResponse>() {
                    @Override
                    public void onResponse(Call<CreateRequestResponse> call, Response<CreateRequestResponse> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(getContext(), "Request submitted! ID: " + response.body().getId(), Toast.LENGTH_SHORT).show();
                            resetForm();
                        } else {
                            Toast.makeText(getContext(), "Submission failed!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CreateRequestResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetForm() {
        etDescription.setText("");
        etBillAmount.setText("");
        spinnerBillType.setSelection(0);
        imgPreview.setImageDrawable(null);
        imgPreview.setVisibility(View.GONE);
        fileUri = null;
        fileBase64 = null;
        fileMimeType = null;
    }
}
