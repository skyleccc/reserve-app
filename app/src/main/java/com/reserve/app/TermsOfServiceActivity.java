package com.reserve.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TermsOfServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termsofservice);

        TextView textTerms = findViewById(R.id.textTerms);
        Button btnBack = findViewById(R.id.btnBack);

        String termsText = "TERMS OF SERVICE\n\n" +
                "Welcome to Reserve! These Terms of Service govern your use of the Reserve mobile application and related services.\n" +
                "\n" +
                "By accessing or using Reserve, you agree to be bound by these Terms. If you do not agree, please do not use the app.\n" +
                "\n" +
                "Eligibility:\n" +
                "You must be at least 18 years old and capable of entering into legally binding contracts to use Reserve. By using the app, you represent that you meet these requirements.\n" +
                "User Accounts:\n" +
                "To use certain features, you must create an account with accurate and complete information. You are responsible for maintaining the confidentiality of your account and password.\n" +
                "Platform Description:\n" +
                "Reserve is a platform that connects individuals seeking parking spaces “Renters” with those offering parking spaces “Space Owners”. We are not a parking operator and do not own or manage any of the spaces listed.\n" +
                "Payments & Fees:\n" +
                "All transactions are processed through the Reserve platform. Reserve charges a service fee per transaction. By using the app, you agree to pay all applicable fees. We use secure third-party payment processors and do not store your financial information.\n" +
                "\n" +
                "Cancellations & Refunds:\n" +
                "Cancellation and refund policies vary depending on the listing and timing of the cancellation. Users are encouraged to review the cancellation policy for each space prior to booking.\n" +
                "User Responsibilities \n" +
                "Renters must vacate the space on time and follow all posted rules.\n" +
                "Space Owners must ensure their listings are accurate and that the space is available and accessible during booked times.\n" +
                "Users agree not to use the app for any unlawful or harmful activity.\n" +
                "Prohibited Conduct:\n" +
                "Post false or misleading information\n" +
                "Use the platform to harass or defraud others\n" +
                "Interfere with the operation of the platform or access other accounts without authorization\n" +
                "Limitation of Liability:\n" +
                "Reserve is not liable for any direct, indirect, incidental, or consequential damages resulting from the use of the app, including issues related to parking availability, safety, or disputes between users\n" +
                "Dispute Resolution:\n" +
                "Disputes between users should be resolved directly. Reserve may, but is not obligated to, mediate in certain cases. For legal disputes, you agree to submit to the jurisdiction of the courts in Qimonda It Center, 6000 Port Service Rd, Cebu City or anywhere near in the City of Cebu.\n";

        textTerms.setText(termsText);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Close the activity
            }
        });
    }
}
