package com.reserve.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class HomepageActivity extends AppCompatActivity {
    private List<ParkingSpot> parkingSpots = new ArrayList<>();
    private List<ParkingSpotWithDistance> allSpotsWithDistance = new ArrayList<>();
    private ParkingSpotAdapter adapter;
    TextView tvParkingTitle;
    private boolean isDataInitialized = false;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final double DEFAULT_MAX_DISTANCE_KM = 5.0; // 5 kilometers default

    // Optimization 1: Geocoder cache to avoid redundant geocoding
    private Map<String, Address> geocodeCache = new HashMap<>();
    private ExecutorService executor = Executors.newFixedThreadPool(4);

    // Optimization 5: Cache for Firestore data
    private List<ParkingSpot> parkingSpotCache;

    // Counter for geocoding completions
    private int geocodingCounter = 0;
    private int totalGeocodingTasks = 0;

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
        tvParkingTitle = findViewById(R.id.tv_parking_spots_title);
        RecyclerView parkingList = findViewById(R.id.parking_list);
        parkingList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParkingSpotAdapter(this, parkingSpots);
        parkingList.setAdapter(adapter);

        // Bottom nav click listeners
        LinearLayout navExplore = findViewById(R.id.nav_explore);
        LinearLayout navSaved = findViewById(R.id.nav_saved);
        LinearLayout navUpdates = findViewById(R.id.nav_updates);
        LinearLayout navAdd = findViewById(R.id.nav_add);
        ShapeableImageView navProfile = findViewById(R.id.iv_profile);

        // Request location permissions if needed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // Optimization 4: More efficient location tracking
            startLocationTracking();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        // Initial load parking spots
        if (!isDataInitialized) {
            // Optimization 5: Using cache
            loadNearbyParkingSpots(true); // true indicates initial load
        }

        // Setup search functionality
        setupSearchBar();

        navExplore.setOnClickListener(v -> {
            // Already on explore page
        });

        navSaved.setOnClickListener(v -> {
            // Navigate to saved
        });

        navUpdates.setOnClickListener(v -> {
            // Navigate to updates
        });

        navAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddLocationActivity.class));
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Optimization 4: Only start location tracking if we don't already have a location
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
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown executor service
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
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

    // Optimization 4: Improved location tracking
    private void startLocationTracking() {
        SessionManager sessionManager = new SessionManager(this);
        ReserveApplication app = (ReserveApplication) getApplicationContext();

        // Only start if we don't have a location yet
        if (sessionManager.getUserLat() == 0 && sessionManager.getUserLng() == 0) {
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
                app.getLocationTracker().stopLocationUpdates();
            });
        } else {
            // Use cached location and update in background
            loadNearbyParkingSpots(false);
        }
    }

    // Optimization 5: Cache for Firestore data
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

        // Check cache first
        if (parkingSpotCache != null && !parkingSpotCache.isEmpty() && !showLoadingIndicator) {
            processParkingSpots(parkingSpotCache, userLat, userLng, showLoadingIndicator);
            // Refresh cache in background
            refreshParkingSpotsCache(false);
            return;
        }

        // Query all parking spaces from Firestore with caching
        FirebaseFirestore.getInstance().collection("parking_spaces")
                .get(Source.SERVER)  // Try cache first, then network
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ParkingSpot> newSpots = new ArrayList<>();

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
                    }

                    // Update cache
                    parkingSpotCache = new ArrayList<>(newSpots);

                    // Process spots
                    processParkingSpots(newSpots, userLat, userLng, showLoadingIndicator);

                    // Hide loading indicator on success
                    if (showLoadingIndicator) {
                        progressBar.setVisibility(View.GONE);
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

    // Helper method to refresh cache in background
    private void refreshParkingSpotsCache(boolean showLoading) {
        FirebaseFirestore.getInstance().collection("parking_spaces")
                .get(Source.SERVER)  // Force server refresh
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ParkingSpot> newSpots = new ArrayList<>();

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
                    }

                    // Update cache for next time
                    parkingSpotCache = new ArrayList<>(newSpots);
                });
    }

    private void processParkingSpots(List<ParkingSpot> spots, double userLat, double userLng,
                                     boolean showLoadingIndicator) {
        // Reset counters
        geocodingCounter = 0;
        totalGeocodingTasks = spots.size();

        // Reset the list that will hold spots with distance
        List<ParkingSpotWithDistance> newSpotsWithDistance = new ArrayList<>();

        // Show loading only if flag is set
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        if (showLoadingIndicator) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Process each spot
        for (ParkingSpot spot : spots) {
            geocodeAddressAndAddToList(spot, spot.address, userLat, userLng,
                    newSpotsWithDistance, spots);
        }
    }

    // Optimization 3: More efficient data filtering
    private void filterAndUpdateParkingSpotsList(List<ParkingSpotWithDistance> spotsWithDistance, double maxDistanceKm) {
        // Store all spots with distance for searching
        allSpotsWithDistance = new ArrayList<>(spotsWithDistance);

        // Optimization 3: More efficient filtering using streams
        List<ParkingSpot> filteredSpots = spotsWithDistance.stream()
                .filter(spd -> spd.distance <= maxDistanceKm)
                .sorted(Comparator.comparingDouble(spd -> spd.distance))
                .map(spd -> spd.spot)
                .collect(Collectors.toList());

        // Only update if we have new data
        if (!filteredSpots.isEmpty()) {
            // Update the main list
            parkingSpots.clear();
            parkingSpots.addAll(filteredSpots);

            // Update the existing adapter
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    // Optimization 1: Improved geocoding with caching and background processing
    private void geocodeAddressAndAddToList(ParkingSpot spot, String address, double userLat, double userLng,
                                             List<ParkingSpotWithDistance> spotsWithDistance, List<ParkingSpot> spots) {
        try {
            // Create a cache for geocoded addresses
            if (geocodeCache.containsKey(address)) {
                // Use cached coordinates
                processGeocodedLocation(spot, geocodeCache.get(address), userLat, userLng, spotsWithDistance, spots);
                return;
            }

            // Show loading indicator for first geocoding request only
            if (geocodingCounter == 0 && spotsWithDistance.isEmpty()) {
                runOnUiThread(() -> findViewById(R.id.progress_bar).setVisibility(View.VISIBLE));
            }

            // Run geocoding in background thread
            executor.execute(() -> {
                try {
                    Geocoder geocoder = new Geocoder(HomepageActivity.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocationName(address, 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        // Cache the result
                        geocodeCache.put(address, addresses.get(0));

                        // Process on main thread
                        runOnUiThread(() ->
                                processGeocodedLocation(spot, addresses.get(0), userLat, userLng,
                                        spotsWithDistance, spots)
                        );
                    } else {
                        // Handle failed geocoding
                        runOnUiThread(() -> incrementGeocodingCounter(spotsWithDistance, spots));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Ensure counter still increments on error
                    runOnUiThread(() -> incrementGeocodingCounter(spotsWithDistance, spots));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            incrementGeocodingCounter(spotsWithDistance, spots);
        }
    }

    private void processGeocodedLocation(ParkingSpot spot, Address location, double userLat, double userLng,
                                         List<ParkingSpotWithDistance> spotsWithDistance, List<ParkingSpot> spots) {
        double spotLat = location.getLatitude();
        double spotLng = location.getLongitude();

        // Calculate distance between user and parking spot
        double distance = calculateDistance(userLat, userLng, spotLat, spotLng);

        spotsWithDistance.add(new ParkingSpotWithDistance(spot, distance));

        // Increment counter and check if all geocoding is done
        incrementGeocodingCounter(spotsWithDistance, spots);
    }

    private synchronized void incrementGeocodingCounter(List<ParkingSpotWithDistance> spotsWithDistance, List<ParkingSpot> spots) {
        geocodingCounter++;

        // Check if all geocoding tasks are completed
        if (geocodingCounter >= totalGeocodingTasks) {
            // Sort by distance
            Collections.sort(spotsWithDistance, Comparator.comparingDouble(spd -> spd.distance));

            // Update UI with results
            filterAndUpdateParkingSpotsList(spotsWithDistance, DEFAULT_MAX_DISTANCE_KM);

            // Mark data as initialized
            isDataInitialized = true;

            // Hide progress bar
            findViewById(R.id.progress_bar).setVisibility(View.GONE);
        }
    }

    private void setupSearchBar() {
        EditText searchEditText = findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterParkingSpots(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Make search bar clickable to open keyboard
        LinearLayout searchBar = findViewById(R.id.search_bar);
        searchBar.setClickable(true);
        searchBar.setFocusable(true);
        searchBar.setOnClickListener(v -> {
            // Set focus to the EditText
            searchEditText.requestFocus();

            // Show the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        });

        // Make map container clickable to open map
        CardView searchMap = findViewById(R.id.map_button_container);
        searchMap.setOnClickListener(v -> {
            openMapView();
        });
    }

    private void openMapView() {
        startActivity(new Intent(this, MapSearchActivity.class));
    }

    // Optimization 3: More efficient filtering
    private void filterParkingSpots(String query) {
        if (query.isEmpty()) {
            // If search is empty, restore filtered spots based on distance
            tvParkingTitle.setText("Parking Spots Near You");

            // Use stream for more efficient filtering
            List<ParkingSpot> filteredSpots = allSpotsWithDistance.stream()
                    .filter(spd -> spd.distance <= DEFAULT_MAX_DISTANCE_KM)
                    .sorted(Comparator.comparingDouble(spd -> spd.distance))
                    .map(spd -> spd.spot)
                    .collect(Collectors.toList());

            adapter.updateSpots(filteredSpots);
            return;
        }

        // Change title to show we're displaying search results
        tvParkingTitle.setText("Search Results");

        // Filter spots by name using stream
        final String lowercaseQuery = query.toLowerCase();
        List<ParkingSpot> filteredSpots = allSpotsWithDistance.stream()
                .filter(spd -> spd.spot.title.toLowerCase().contains(lowercaseQuery))
                .sorted(Comparator.comparingDouble(spd -> spd.distance))
                .map(spd -> spd.spot)
                .collect(Collectors.toList());

        adapter.updateSpots(filteredSpots);
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