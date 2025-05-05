package com.reserve.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {
    LinearLayout accountBtn;
    Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        accountBtn = findViewById(R.id.accountLayout);
        logoutBtn = findViewById(R.id.logoutBtn);

        accountBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditUser.class));
        });

        logoutBtn.setOnClickListener(v -> {
            getSharedPreferences("app", MODE_PRIVATE).edit().putBoolean("logged_in", false).apply();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
    }
}