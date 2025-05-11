package com.reserve.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;

public class AddLocationActivity extends AppCompatActivity {
    TextView tvHeader, tvExplore, tvSaved, tvUpdates, tvAdd;
    LinearLayout navExplore, navSaved, navUpdates, navAdd;
    ShapeableImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_location);

        // Profile click listener
        ivProfile = findViewById(R.id.iv_profile);

        // Bottom nav click listeners
        navExplore = findViewById(R.id.nav_explore);
        navSaved = findViewById(R.id.nav_saved);
        navUpdates = findViewById(R.id.nav_updates);
        navAdd = findViewById(R.id.nav_add);

        // Initialize views
        tvHeader = findViewById(R.id.tv_header);
        ivProfile = findViewById(R.id.iv_profile);
        tvExplore = findViewById(R.id.tv_explore);
        tvSaved = findViewById(R.id.tv_saved);
        tvUpdates = findViewById(R.id.tv_updates);
        tvAdd = findViewById(R.id.tv_add);

        ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        navExplore.setOnClickListener(v -> {
            startActivity(new Intent(this, HomepageActivity.class));
        });

        navSaved.setOnClickListener(v -> {
            startActivity(new Intent(this, HomepageActivity.class));
        });

        navUpdates.setOnClickListener(v -> {
            startActivity(new Intent(this, HomepageActivity.class));
        });

        navAdd.setOnClickListener(v -> {

        });
    }
}