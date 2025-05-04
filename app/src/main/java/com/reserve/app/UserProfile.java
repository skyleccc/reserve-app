package com.reserve.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserProfile extends AppCompatActivity {
    ImageButton backBtn, editBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);

        backBtn.setOnClickListener(v -> finish());
        editBtn.setOnClickListener(v -> startActivity(new Intent(UserProfile.this, EditUser.class)));
    }
}