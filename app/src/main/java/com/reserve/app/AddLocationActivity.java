package com.reserve.app;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;

public class AddLocationActivity extends AppCompatActivity {
    TextView tvHeader, tvExplore, tvSaved, tvUpdates, tvAdd;
    ShapeableImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_location);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        tvHeader = findViewById(R.id.tv_header);
        ivProfile = findViewById(R.id.iv_profile);
        tvExplore = findViewById(R.id.tv_explore);
        tvSaved = findViewById(R.id.tv_saved);
        tvUpdates = findViewById(R.id.tv_updates);
        tvAdd = findViewById(R.id.tv_add);
    }
}