package com.reserve.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {
    LinearLayout accountBtn, privacyPolicyLayout, whatsNewLayout, faqLayout, termsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        accountBtn = findViewById(R.id.accountLayout);
        whatsNewLayout = findViewById(R.id.whatsNewLayout);
        faqLayout = findViewById(R.id.faqLayout);
        termsLayout = findViewById(R.id.termsLayout);
        privacyPolicyLayout = findViewById(R.id.privacyPolicyLayout);

        accountBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditUser.class));
        });

        // What's New - Genald
        whatsNewLayout.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.whats_new_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setView(dialogView);
            builder.setCancelable(true);
            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        // FAQ - Genald
        faqLayout.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.faq_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setView(dialogView);
            builder.setCancelable(true);
            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        // Terms of Service - Genald
        termsLayout.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.terms_of_service_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setView(dialogView);
            builder.setCancelable(true);
            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        //Privacy Policy - Genald
        privacyPolicyLayout.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.privacy_policy_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setView(dialogView);
            builder.setCancelable(true);
            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });

    }
}