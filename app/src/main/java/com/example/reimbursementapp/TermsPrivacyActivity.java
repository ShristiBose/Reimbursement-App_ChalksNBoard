
package com.example.reimbursementapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TermsPrivacyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_privacy);

        TextView tvContent = findViewById(R.id.tvContent);

        String content = "📄 TERMS & CONDITIONS\n\n" +
                "1. Eligibility\n" +
                "- This Reimbursement App is for authorized employees, team leads, and admins of [Your Company Name].\n\n" +

                "2. Use of the App\n" +
                "- Users must provide accurate reimbursement claims with valid supporting documents.\n" +
                "- Fraudulent claims may lead to rejection and possible disciplinary action.\n" +
                "- The company reserves the right to verify and approve/reject claims.\n\n" +

                "3. Roles & Approvals\n" +
                "- Staff submit claims.\n" +
                "- Team Leads review and validate claims.\n" +
                "- Admins finalize approvals. The company’s decision is final.\n\n" +

                "4. Restrictions\n" +
                "- Users must not misuse the app.\n" +
                "- Uploading false or misleading receipts is prohibited.\n\n" +

                "5. Liability\n" +
                "- The company is not liable for delays caused by network, technical errors, or incorrect submissions.\n\n" +

                "6. Updates\n" +
                "- The company may update these Terms & Conditions. Continued use of the app means acceptance of changes.\n\n\n" +

                "🔒 PRIVACY POLICY\n\n" +

                "1. Information We Collect\n" +
                "- Personal details: Name, email, employee ID.\n" +
                "- Uploaded receipts/bills for reimbursement.\n" +
                "- App usage data (logins, activity).\n\n" +

                "2. How We Use Your Data\n" +
                "- To process reimbursement requests.\n" +
                "- To verify claims and manage approvals.\n" +
                "- To improve app security and functionality.\n\n" +

                "3. Data Sharing\n" +
                "- Data is shared only with authorized personnel (team leads, admins).\n" +
                "- We do not sell or rent your data to third parties.\n\n" +

                "4. Data Security\n" +
                "- Your information is stored securely with restricted access.\n" +
                "- We take reasonable measures to protect your data.\n\n" +

                "5. Your Rights\n" +
                "- You may request correction or deletion of your data.\n" +
                "- For questions, contact: support@yourcompany.com\n\n" +

                "Effective Date: [Insert Date]\n\n" +
                "© [Your Company Name]. All rights reserved.";

        tvContent.setText(content);
    }
}
