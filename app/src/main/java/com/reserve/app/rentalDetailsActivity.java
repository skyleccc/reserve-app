package com.reserve.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class rentalDetailsActivity extends AppCompatActivity {

    private TextView tvLocationName, tvAddress, tvLicensePlate, tvVehicleDesc, tvDuration, tvEndTime;
    private Button btnExtendTime, btnSaveLocation, btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rental_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        tvLocationName = findViewById(R.id.tv_location_name);
        tvAddress = findViewById(R.id.tv_address);
        tvLicensePlate = findViewById(R.id.tv_license_plate);
        tvVehicleDesc = findViewById(R.id.tv_vehicle_desc);
        tvDuration = findViewById(R.id.tv_duration);
        tvEndTime = findViewById(R.id.tv_end_time);
        btnExtendTime = findViewById(R.id.btn_extend_time);
        btnSaveLocation = findViewById(R.id.btn_save_location);
        btnHome = findViewById(R.id.btn_home);

        // Get data from intent
        String spotName = getIntent().getStringExtra("SPOT_NAME");
        String spotLocation = getIntent().getStringExtra("SPOT_LOCATION");
        String licensePlate = getIntent().getStringExtra("LICENSE_PLATE");
        String vehicleDesc = getIntent().getStringExtra("VEHICLE_DESC");
        int duration = getIntent().getIntExtra("DURATION", 3);
        String paymentMethod = getIntent().getStringExtra("PAYMENT_METHOD");

        // Calculate end time
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm", Locale.getDefault());
        SimpleDateFormat fullSdf = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        Date startTime = calendar.getTime();
        calendar.add(Calendar.HOUR, duration);
        Date endTime = calendar.getTime();

        // Set data to views
        tvLocationName.setText("Near " + spotName);
        tvAddress.setText(spotLocation);
        tvLicensePlate.setText(licensePlate);
        tvVehicleDesc.setText(vehicleDesc);
        tvDuration.setText(duration + " hour" + (duration > 1 ? "s" : ""));
        tvEndTime.setText("Ends at " + sdf.format(endTime) + " on " + fullSdf.format(endTime));

        // Set up button click listeners
        btnExtendTime.setOnClickListener(v -> {
            Toast.makeText(rentalDetailsActivity.this,
                    "Extend time feature will be implemented soon", Toast.LENGTH_SHORT).show();
        });

        btnSaveLocation.setOnClickListener(v -> {
            Toast.makeText(rentalDetailsActivity.this,
                    "Location saved to favorites", Toast.LENGTH_SHORT).show();
        });

        btnHome.setOnClickListener(v -> {
            // Navigate to home screen
            finish();
        });

        // Set up bottom navigation
        View exploreBtn = findViewById(R.id.nav_explore);
        View savedBtn = findViewById(R.id.nav_saved);
        View updatesBtn = findViewById(R.id.nav_updates);
        View addBtn = findViewById(R.id.nav_add);

        exploreBtn.setOnClickListener(v -> navigateToTab("Explore"));
        savedBtn.setOnClickListener(v -> navigateToTab("Saved"));
        updatesBtn.setOnClickListener(v -> navigateToTab("Updates"));
        addBtn.setOnClickListener(v -> navigateToTab("Add"));
    }

    private void navigateToTab(String tabName) {
        Toast.makeText(this, tabName + " tab clicked", Toast.LENGTH_SHORT).show();
        // Implementation for navigation would go here
    }
}