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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class rentalDetailsActivity extends AppCompatActivity {

    private TextView tvLocationName, tvAddress, tvLicensePlate, tvVehicleDesc, tvDuration, tvEndTime;
    private Button btnExtendTime, btnSaveLocation, btnHome;
    private DatabaseHandler dbHandler;
    private String spotId;
    private boolean isSpotSaved = false;

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

        // Initialize database handler
        dbHandler = DatabaseHandler.getInstance(this);

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
        spotId = getIntent().getStringExtra("SPOT_ID");
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
        tvLocationName.setText(spotName);
        tvAddress.setText(spotLocation);
        tvLicensePlate.setText(licensePlate);
        tvVehicleDesc.setText(vehicleDesc);
        tvDuration.setText(duration + " hour" + (duration > 1 ? "s" : ""));
        tvEndTime.setText("Ends at " + sdf.format(endTime) + " on " + fullSdf.format(endTime));

        // Check if spot is already saved
        checkSavedStatus();

        // Set up button click listeners
        btnExtendTime.setOnClickListener(v -> {
            showExtendTimeDialog();
        });

        btnSaveLocation.setOnClickListener(v -> toggleSaveLocation());

        btnHome.setOnClickListener(v -> {
            // Navigate to home screen
            Intent intent = new Intent(rentalDetailsActivity.this, HomepageActivity.class);
            startActivity(intent);
            finish(); // finish this activity
        });

        // Set up bottom navigation
        View exploreBtn = findViewById(R.id.nav_explore);
        View savedBtn = findViewById(R.id.nav_saved);
        View updatesBtn = findViewById(R.id.nav_updates);
        View addBtn = findViewById(R.id.nav_add);

        exploreBtn.setOnClickListener(v -> {
            Intent intent = new Intent(rentalDetailsActivity.this, MainActivity.class);
            startActivity(intent);
        });
        savedBtn.setOnClickListener(v -> {
            Intent intent = new Intent(rentalDetailsActivity.this, SavedSpotsActivity.class);
            startActivity(intent);
        });
        updatesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(rentalDetailsActivity.this, MainActivity.class);
            startActivity(intent);
        });
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(rentalDetailsActivity.this, AddLocationActivity.class);
            startActivity(intent);
        });
    }

    private void checkSavedStatus() {
        if (spotId == null || spotId.isEmpty()) {
            Toast.makeText(this, "Invalid parking spot information", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHandler.checkIfSpotIsSaved(spotId, new DatabaseHandler.BooleanCallback() {
            @Override
            public void onResult(boolean result) {
                isSpotSaved = result;
                updateSaveButton();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(rentalDetailsActivity.this,
                        "Error checking saved status: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSaveButton() {
        if (isSpotSaved) {
            btnSaveLocation.setText("★ Remove from saved");
        } else {
            btnSaveLocation.setText("⭐ Save location");
        }
    }

    private void toggleSaveLocation() {
        if (spotId == null || spotId.isEmpty()) {
            Toast.makeText(this, "Invalid parking spot information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create ParkingSpot object with basic information needed
        ParkingSpot spot = new ParkingSpot(
                spotId,
                tvLocationName.getText().toString(),
                tvAddress.getText().toString(),
                R.drawable.ic_map_placeholder,
                "", "", "", "");

        dbHandler.saveParkingSpot(spot, new DatabaseHandler.BooleanCallback() {
            @Override
            public void onResult(boolean result) {
                isSpotSaved = result;
                updateSaveButton();

                if (result) {
                    Toast.makeText(rentalDetailsActivity.this,
                            "Location saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(rentalDetailsActivity.this,
                            "Location removed from saved", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(rentalDetailsActivity.this,
                        "Error saving location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showExtendTimeDialog() {
        final String[] timeOptions = {"1 hour", "3 hours", "6 hours", "12 hours"};
        final int[] timeValues = {1, 3, 6, 12};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Extend Parking Time");
        builder.setItems(timeOptions, (dialog, which) -> {
            int additionalHours = timeValues[which];
            extendParkingTime(additionalHours);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void extendParkingTime(int additionalHours) {
        // Parse current duration from TextView
        String durationText = tvDuration.getText().toString();
        int currentDuration = Integer.parseInt(durationText.split(" ")[0]);
        int newDuration = currentDuration + additionalHours;

        // Calculate new end time
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, newDuration); // New total duration from now
        Date newEndTime = calendar.getTime();

        // Format for display
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm", Locale.getDefault());
        SimpleDateFormat fullSdf = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

        // Format for database
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String endTimeString = dbFormat.format(newEndTime);

        // Update rental in database
        updateRentalEndTime(endTimeString, additionalHours);

        // Update UI immediately to show the change
        tvDuration.setText(newDuration + " hour" + (newDuration > 1 ? "s" : ""));
        tvEndTime.setText("Ends at " + sdf.format(newEndTime) + " on " + fullSdf.format(newEndTime));

        Toast.makeText(this, "Extended time by " + additionalHours + " hour" +
                (additionalHours > 1 ? "s" : ""), Toast.LENGTH_SHORT).show();
    }

    private void updateRentalEndTime(String newEndTime, int additionalHours) {
        int currentDuration = Integer.parseInt(tvDuration.getText().toString().split(" ")[0]);

        dbHandler.extendRentalTime(spotId, newEndTime, currentDuration, additionalHours, new DatabaseHandler.BooleanCallback() {
            @Override
            public void onResult(boolean result) {
                if (result) {
                    Toast.makeText(rentalDetailsActivity.this,
                            "Rental extended successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(rentalDetailsActivity.this,
                            "Failed to update rental", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(rentalDetailsActivity.this,
                        "Error updating rental: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}