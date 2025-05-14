package com.reserve.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private DatabaseHandler dbHandler;
    private String spotId, spotName, spotLocation, price3h, price6h, price12h, price24h;
    private TextView rate3hTextView, rate6hTextView, rate12hTextView, rate24hTextView, tvTotalAmount;
    private CardView duration3hCard, duration6hCard, duration12hCard, duration24hCard;
    private ImageButton btnBookmark;
    private boolean isSaved = false;
    private int selectedDuration = 3;
    private GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 15f;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng parkingSpotLatLng;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enable back button in ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize DatabaseHandler
        dbHandler = DatabaseHandler.getInstance(this);

        // Retrieve parking spot details from Intent
        spotId = getIntent().getStringExtra("SPOT_ID");
        spotName = getIntent().getStringExtra("SPOT_NAME");
        spotLocation = getIntent().getStringExtra("SPOT_LOCATION");
        price3h = getIntent().getStringExtra("PRICE_3H");
        price6h = getIntent().getStringExtra("PRICE_6H");
        price12h = getIntent().getStringExtra("PRICE_12H");
        price24h = getIntent().getStringExtra("PRICE_24H");

        // Initialize UI components
        TextView tvSpotName = findViewById(R.id.tv_parking_spot_name);
        TextView tvSpotLocation = findViewById(R.id.tv_parking_spot_address);
        Button btnBookNow = findViewById(R.id.book_now_button);
        ImageButton backButton = findViewById(R.id.back_button);
        rate3hTextView = findViewById(R.id.rate_3h);
        rate6hTextView = findViewById(R.id.rate_6h);
        rate12hTextView = findViewById(R.id.rate_12h);
        rate24hTextView = findViewById(R.id.rate_24h);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnBookmark = findViewById(R.id.btn_bookmark);

        // Check if spot is saved
        checkIfSpotIsSaved();

        // Set parking spot details
        tvSpotName.setText(spotName);
        tvSpotLocation.setText(spotLocation);
        rate3hTextView.setText(price3h);
        rate6hTextView.setText(price6h);
        rate12hTextView.setText(price12h);
        rate24hTextView.setText(price24h);

        // Set back button click listener
        backButton.setOnClickListener(v -> finish());

        // Set click listener for bookmark button
        btnBookmark.setOnClickListener(v -> toggleSaveSpot());

        // Set up "Book Now" button click listener
        btnBookNow.setOnClickListener(v -> bookParkingSpot());

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fetchParkingSpotOwnerInfo();
        setupDurationCards();
    }

    private void checkIfSpotIsSaved() {
        if (spotId == null || spotId.isEmpty()) {
            return;
        }

        dbHandler.checkIfSpotIsSaved(spotId, new DatabaseHandler.BooleanCallback() {
            @Override
            public void onResult(boolean result) {
                isSaved = result;
                updateBookmarkIcon();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(BookingActivity.this,
                        "Error checking saved status: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBookmarkIcon() {
        if (isSaved) {
            btnBookmark.setColorFilter(Color.parseColor("#FFC107")); // Yellow for saved
        } else {
            btnBookmark.setColorFilter(Color.parseColor("#757575")); // Gray for not saved
        }
    }

    private void toggleSaveSpot() {
        if (spotId == null || spotId.isEmpty()) {
            return;
        }

        // Create a ParkingSpot object with the available information
        ParkingSpot spot = new ParkingSpot(
                spotId,
                spotName,
                spotLocation,
                R.drawable.ic_map_placeholder,
                price3h,
                price6h,
                price12h,
                price24h
        );

        dbHandler.saveParkingSpot(spot, new DatabaseHandler.BooleanCallback() {
            @Override
            public void onResult(boolean result) {
                isSaved = result; // result is true if saved, false if removed
                updateBookmarkIcon();

                String message = result ?
                        "Added to saved spots" :
                        "Removed from saved spots";
                Toast.makeText(BookingActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(BookingActivity.this,
                        "Error updating saved status: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle back button click
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable my location if permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        // Geocode the parking spot address and show on map
        if (spotLocation != null && !spotLocation.isEmpty()) {
            geocodeAndShowOnMap(spotLocation);
        }
    }

    private void geocodeAndShowOnMap(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                parkingSpotLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                // Add marker for the parking spot
                mMap.addMarker(new MarkerOptions()
                        .position(parkingSpotLatLng)
                        .title(spotName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                // Get user location and show both on map
                showUserLocationAndRoute();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Could not locate address on map", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showUserLocationAndRoute() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && parkingSpotLatLng != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                // Calculate distance
                float[] results = new float[1];
                Location.distanceBetween(
                        userLatLng.latitude, userLatLng.longitude,
                        parkingSpotLatLng.latitude, parkingSpotLatLng.longitude,
                        results);
                float distanceInKm = results[0] / 1000;

                // Draw simple route line
                mMap.addPolyline(new PolylineOptions()
                        .add(userLatLng, parkingSpotLatLng)
                        .width(5)
                        .color(Color.BLUE));

                // Create bounds to include both points
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                boundsBuilder.include(userLatLng);
                boundsBuilder.include(parkingSpotLatLng);
                LatLngBounds bounds = boundsBuilder.build();

                // Move camera to show both locations with padding
                int padding = 200; // pixels
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                // Show distance in a toast
                Toast.makeText(this, String.format("Distance: %.2f km", distanceInKm),
                        Toast.LENGTH_SHORT).show();
            } else if (parkingSpotLatLng != null) {
                // If user location not available, just show parking spot
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(parkingSpotLatLng, DEFAULT_ZOOM));
            }
        });
    }

    private void fetchParkingSpotOwnerInfo() {
        if (spotId == null || spotId.isEmpty()) {
            return;
        }

        // First get the parking spot to get the owner's userId
        dbHandler.getParkingSpotById(spotId, new DatabaseHandler.ParkingSpotCallback() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                if (document.exists()) {
                    String ownerId = document.getString("userId");
                    if (ownerId != null && !ownerId.isEmpty()) {
                        // Now fetch the owner's details using userId
                        dbHandler.getUserData(ownerId, new DatabaseHandler.DocumentCallback() {
                            @Override
                            public void onSuccess(DocumentSnapshot userDoc) {
                                if (userDoc.exists()) {
                                    // Update UI with owner information
                                    String firstName = userDoc.getString("firstName");
                                    String lastName = userDoc.getString("lastName");
                                    String phone = userDoc.getString("phone");

                                    TextView ownerNameTextView = findViewById(R.id.owner_name);
                                    TextView ownerPhoneTextView = findViewById(R.id.owner_phone);

                                    if (firstName != null && lastName != null) {
                                        ownerNameTextView.setText(firstName + " " + lastName);
                                    }

                                    if (phone != null) {
                                        ownerPhoneTextView.setText(phone);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(BookingActivity.this,
                                        "Failed to load owner information", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(BookingActivity.this,
                        "Failed to load parking spot details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        showUserLocationAndRoute();
                    }
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void bookParkingSpot() {
        // Process booking directly using the selected duration
        int selectedOption;
        switch (selectedDuration) {
            case 3:
                selectedOption = 0;
                break;
            case 6:
                selectedOption = 1;
                break;
            case 12:
                selectedOption = 2;
                break;
            case 24:
                selectedOption = 3;
                break;
            default:
                selectedOption = 0; // Default to 3 hours
                break;
        }

        processBooking(selectedOption);
    }

    private void setupDurationCards() {
        duration3hCard = findViewById(R.id.duration_3h_card);
        duration6hCard = findViewById(R.id.duration_6h_card);
        duration12hCard = findViewById(R.id.duration_12h_card);
        duration24hCard = findViewById(R.id.duration_24h_card);

        duration3hCard.setOnClickListener(v -> selectDurationCard(3));
        duration6hCard.setOnClickListener(v -> selectDurationCard(6));
        duration12hCard.setOnClickListener(v -> selectDurationCard(12));
        duration24hCard.setOnClickListener(v -> selectDurationCard(24));

        // Set default selection
        selectDurationCard(3);
    }

    private void selectDurationCard(int hours) {
        // Reset all cards - using light purple color for non-selected cards
        duration3hCard.setCardBackgroundColor(Color.parseColor("#ece6f0"));
        duration6hCard.setCardBackgroundColor(Color.parseColor("#ece6f0"));
        duration12hCard.setCardBackgroundColor(Color.parseColor("#ece6f0"));
        duration24hCard.setCardBackgroundColor(Color.parseColor("#ece6f0"));

        // Find all text views and reset color
        ((TextView)duration3hCard.getChildAt(0)).setTextColor(Color.parseColor("#757575"));
        ((TextView)duration6hCard.getChildAt(0)).setTextColor(Color.parseColor("#757575"));
        ((TextView)duration12hCard.getChildAt(0)).setTextColor(Color.parseColor("#757575"));
        ((TextView)duration24hCard.getChildAt(0)).setTextColor(Color.parseColor("#757575"));

        // Highlight selected card
        int sbtnColor = Color.parseColor("#E3F2FD");
        int stextColor = Color.parseColor("#000000");

        // Update total amount based on selected duration
        switch (hours) {
            case 3:
                duration3hCard.setCardBackgroundColor(sbtnColor);
                ((TextView)duration3hCard.getChildAt(0)).setTextColor(stextColor);
                tvTotalAmount.setText(price3h);
                break;
            case 6:
                duration6hCard.setCardBackgroundColor(sbtnColor);
                ((TextView)duration6hCard.getChildAt(0)).setTextColor(stextColor);
                tvTotalAmount.setText(price6h);
                break;
            case 12:
                duration12hCard.setCardBackgroundColor(sbtnColor);
                ((TextView)duration12hCard.getChildAt(0)).setTextColor(stextColor);
                tvTotalAmount.setText(price12h);
                break;
            case 24:
                duration24hCard.setCardBackgroundColor(sbtnColor);
                ((TextView)duration24hCard.getChildAt(0)).setTextColor(stextColor);
                tvTotalAmount.setText(price24h);
                break;
        }

        selectedDuration = hours;
    }

    private void processBooking(int selectedOption) {
        // Get vehicle information first
        String licensePlate = ((com.google.android.material.textfield.TextInputEditText) findViewById(R.id.et_license_plate)).getText().toString().trim();
        String vehicleDescription = ((com.google.android.material.textfield.TextInputEditText) findViewById(R.id.et_vehicle_description)).getText().toString().trim();

        // Validate vehicle information
        if (licensePlate.isEmpty() || vehicleDescription.isEmpty()) {
            Toast.makeText(this, "Please enter vehicle information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate start time (now)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startTime = sdf.format(new Date());

        // Calculate end time based on selected option
        Calendar calendar = Calendar.getInstance();
        int durationHours = 3; // Default
        switch (selectedOption) {
            case 0: // 3 Hours
                durationHours = 3;
                calendar.add(Calendar.HOUR, 3);
                break;
            case 1: // 6 Hours
                durationHours = 6;
                calendar.add(Calendar.HOUR, 6);
                break;
            case 2: // 12 Hours
                durationHours = 12;
                calendar.add(Calendar.HOUR, 12);
                break;
            case 3: // 24 Hours
                durationHours = 24;
                calendar.add(Calendar.HOUR, 24);
                break;
        }
        String endTime = sdf.format(calendar.getTime());

        // Get rate based on selected option
        String rateText;
        switch (selectedOption) {
            case 0: rateText = price3h; break;
            case 1: rateText = price6h; break;
            case 2: rateText = price12h; break;
            case 3: rateText = price24h; break;
            default: rateText = price3h;
        }

        double cost = extractCostFromText(rateText);

        // Store these values for use in the intent after successful booking
        final int finalDurationHours = durationHours;
        final String finalLicensePlate = licensePlate;
        final String finalVehicleDescription = vehicleDescription;

        // Call create rental with all required parameters
        dbHandler.createRental(
                spotId,
                startTime,
                endTime,
                cost,
                "Active",
                licensePlate,
                vehicleDescription,
                new DatabaseHandler.BooleanCallback() {
                    @Override
                    public void onResult(boolean result) {
                        if (result) {
                            Toast.makeText(BookingActivity.this, "Booking successful!", Toast.LENGTH_SHORT).show();

                            // Navigate to rental details screen with booking information
                            Intent intent = new Intent(BookingActivity.this, rentalDetailsActivity.class);
                            intent.putExtra("SPOT_ID", spotId);
                            intent.putExtra("SPOT_NAME", spotName);
                            intent.putExtra("SPOT_LOCATION", spotLocation);
                            intent.putExtra("LICENSE_PLATE", finalLicensePlate);
                            intent.putExtra("VEHICLE_DESC", finalVehicleDescription);
                            intent.putExtra("DURATION", finalDurationHours);
                            intent.putExtra("PAYMENT_METHOD", "Credit Card"); // Assuming default payment method
                            startActivity(intent);

                            // Close this activity
                            finish();
                        } else {
                            Toast.makeText(BookingActivity.this, "Booking failed.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        // Booking failed
                        runOnUiThread(() -> Toast.makeText(BookingActivity.this,
                                "Booking failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
        );
    }

    private double extractCostFromText(String rateText) {
        // Remove currency symbol and parse as double
        return Double.parseDouble(rateText.replaceAll("[^\\d.]", ""));
    }
}