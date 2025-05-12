package com.reserve.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomepageActivity extends AppCompatActivity {
    private List<ParkingSpot> parkingSpots = new ArrayList<>();
    private ParkingSpotAdapter adapter;
    private boolean isDataInitialized = false;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final double DEFAULT_MAX_DISTANCE_KM = 5.0; // 5 kilometers default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Parking spots list
        RecyclerView parkingList = findViewById(R.id.parking_list);
        parkingList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParkingSpotAdapter(this, parkingSpots);
        parkingList.setAdapter(adapter);

        // Bottom nav click listeners
        LinearLayout navExplore = findViewById(R.id.nav_explore);
        LinearLayout navSaved = findViewById(R.id.nav_saved);
        LinearLayout navUpdates = findViewById(R.id.nav_updates);
        LinearLayout navAdd = findViewById(R.id.nav_add);

        // Request location permissions if needed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            startLocationTracking();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        // Initial load parking spots
        if (!isDataInitialized) {
            loadNearbyParkingSpots(true); // true indicates initial load
        }

        navExplore.setOnClickListener(v -> {

        });

        navSaved.setOnClickListener(v -> {

        });

        navUpdates.setOnClickListener(v -> {

        });

        navAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddLocationActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only start location tracking if we don't already have a location
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.getUserLat() == 0 && sessionManager.getUserLng() == 0) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                startLocationTracking();
            }
        } else {
            // If we already have data, show it immediately then refresh in background
            if (isDataInitialized) {
                loadNearbyParkingSpots(false);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates when app is in background to save battery
        ReserveApplication app = (ReserveApplication) getApplicationContext();
        app.getLocationTracker().stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationTracking();
            } else {
                Toast.makeText(this, "Location permission needed for nearby parking spots",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationTracking() {
        SessionManager sessionManager = new SessionManager(this);
        ReserveApplication app = (ReserveApplication) getApplicationContext();

        app.startLocationTracking(location -> {
            // Save location to session
            sessionManager.saveUserLocation(location.getLatitude(), location.getLongitude());

            // Save to Firebase if user is logged in
            String userId = sessionManager.getUserId();
            if (userId != null) {
                FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .update("latitude", location.getLatitude(),
                                "longitude", location.getLongitude());
            }

            // Load nearby parking spots after getting location
            loadNearbyParkingSpots(true);

            // Stop continuous updates after getting location once
            // Remove this line if you want continuous tracking
            app.getLocationTracker().stopLocationUpdates();
        });
    }

    private void loadNearbyParkingSpots(boolean showLoadingIndicator) {
        // Get reference to the progress bar
        ProgressBar progressBar = findViewById(R.id.progress_bar);

        // Show loading indicator if needed
        if (showLoadingIndicator) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Get user's current location from SessionManager
        SessionManager sessionManager = new SessionManager(this);
        double userLat = sessionManager.getUserLat();
        double userLng = sessionManager.getUserLng();

        if (userLat == 0 && userLng == 0) {
            if (showLoadingIndicator) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Waiting for your location...", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Create temporary lists to hold new data
        List<ParkingSpot> newSpots = new ArrayList<>();
        List<ParkingSpotWithDistance> newSpotsWithDistance = new ArrayList<>();

        // Set flag to indicate data is initialized
        isDataInitialized = true;

        // Query all parking spaces from Firestore
        FirebaseFirestore.getInstance().collection("parking_spaces")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        // Extract data from document
                        String name = doc.getString("name");
                        String location = doc.getString("location");
                        Double rate3h = doc.getDouble("rate3h");
                        Double rate6h = doc.getDouble("rate6h");
                        Double rate12h = doc.getDouble("rate12h");
                        Double rate24h = doc.getDouble("rate24h");

                        // Format prices
                        String price3Hours = "₱" + String.format("%.2f", rate3h);
                        String price6Hours = "₱" + String.format("%.2f", rate6h);
                        String price12Hours = "₱" + String.format("%.2f", rate12h);
                        String pricePerDay = "₱" + String.format("%.2f", rate24h);

                        // Create parking spot object
                        ParkingSpot spot = new ParkingSpot(name, location,
                                R.drawable.ic_map_placeholder, price3Hours,
                                price6Hours, price12Hours, pricePerDay);

                        newSpots.add(spot);

                        // Geocode the address to get coordinates
                        geocodeAddressAndAddToList(spot, location, userLat, userLng, newSpotsWithDistance, newSpots);
                    }

                    // Hide loading indicator
                    if (showLoadingIndicator) {
                        progressBar.setVisibility(View.GONE);
                    }

                    // If no spots were found
                    if (newSpots.isEmpty() && showLoadingIndicator) {
                        Toast.makeText(HomepageActivity.this, "No parking spots found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator on failure
                    if (showLoadingIndicator) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to load parking spots: " +
                                e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterAndUpdateParkingSpotsList(List<ParkingSpotWithDistance> spotsWithDistance, double maxDistanceKm) {
        // Sort by distance
        Collections.sort(spotsWithDistance, (s1, s2) -> Double.compare(s1.distance, s2.distance));

        // Create a new list for filtered spots
        List<ParkingSpot> filteredSpots = new ArrayList<>();

        // Filter by maximum distance
        for (ParkingSpotWithDistance spd : spotsWithDistance) {
            if (spd.distance <= maxDistanceKm) {
                filteredSpots.add(spd.spot);
            }
        }

        // Only update if we have new data
        if (!filteredSpots.isEmpty()) {
            // Now update the main list
            parkingSpots.clear();
            parkingSpots.addAll(filteredSpots);

            // Update the existing adapter
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            } else {
                // Only create a new adapter if it doesn't exist
                adapter = new ParkingSpotAdapter(this, parkingSpots);
                RecyclerView recyclerView = findViewById(R.id.parking_list);
                recyclerView.setAdapter(adapter);
            }
        }
    }
    private void geocodeAddressAndAddToList(ParkingSpot spot, String address, double userLat, double userLng,
                                            List<ParkingSpotWithDistance> spotsWithDistance, List<ParkingSpot> spots) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                double spotLat = location.getLatitude();
                double spotLng = location.getLongitude();

                // Calculate distance between user and parking spot
                double distance = calculateDistance(userLat, userLng, spotLat, spotLng);

                spotsWithDistance.add(new ParkingSpotWithDistance(spot, distance));

                // When all geocoding is done, sort and update UI
                if (spotsWithDistance.size() == spots.size()) {
                    filterAndUpdateParkingSpotsList(spotsWithDistance, DEFAULT_MAX_DISTANCE_KM);
                    // Hide progress bar when all geocoding is complete
                    ProgressBar progressBar = findViewById(R.id.progress_bar);
                    progressBar.setVisibility(View.GONE);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Also handle errors in geocoding
            if (spotsWithDistance.size() == spots.size()) {
                ProgressBar progressBar = findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    // Helper class to store spot with its distance
    private static class ParkingSpotWithDistance {
        ParkingSpot spot;
        double distance;

        ParkingSpotWithDistance(ParkingSpot spot, double distance) {
            this.spot = spot;
            this.distance = distance;
        }
    }

    // Calculate distance between two coordinates using Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in km
    }
}