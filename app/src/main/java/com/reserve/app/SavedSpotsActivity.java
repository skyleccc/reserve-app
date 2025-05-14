package com.reserve.app;

import android.Manifest;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.location.Address;
        import android.location.Geocoder;
        import android.location.Location;
        import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
        import android.widget.Toast;

        import androidx.activity.EdgeToEdge;
        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;
        import androidx.core.graphics.Insets;
        import androidx.core.view.ViewCompat;
        import androidx.core.view.WindowInsetsCompat;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.google.android.gms.location.FusedLocationProviderClient;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.material.imageview.ShapeableImageView;

        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.Locale;

public class SavedSpotsActivity extends AppCompatActivity {
    RecyclerView savedList;
    private List<ParkingSpot> parkingSpots = new ArrayList<>();
    private SavedSpotsAdapter adapter;
    private List<ParkingSpot> allParkingSpots = new ArrayList<>();
    private EditText searchEditText;
    private DatabaseHandler dbHandler;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_spots);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize location services first thing
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize navigation buttons
        setupNavigation();

        // Initialize RecyclerView and adapter
        savedList = findViewById(R.id.saved_list);
        savedList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavedSpotsAdapter(this, parkingSpots);
        savedList.setAdapter(adapter);

        // Initialize search functionality
        searchEditText = findViewById(R.id.search_edit_text);
        setupSearch();

        // Request location after setup is complete
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        // Fetch saved parking spots
        dbHandler = DatabaseHandler.getInstance(this);
        fetchSavedParkingSpots();
    }

    private void setupNavigation() {
        LinearLayout navExplore = findViewById(R.id.nav_explore);
        LinearLayout navUpdates = findViewById(R.id.nav_updates);
        LinearLayout navAdd = findViewById(R.id.nav_add);
        ShapeableImageView navProfile = findViewById(R.id.iv_profile);

        navExplore.setOnClickListener(v -> startActivity(new Intent(this, HomepageActivity.class)));
        navUpdates.setOnClickListener(v -> startActivity(new Intent(this, RentalListActivity.class)));
        navAdd.setOnClickListener(v -> startActivity(new Intent(this, AddLocationActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterParkingSpots(s.toString());
            }
        });
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Store location in SessionManager
                        SessionManager sessionManager = new SessionManager(this);
                        sessionManager.saveUserLocation(location.getLatitude(), location.getLongitude());

                        // Fetch saved spots with distances
                        fetchSavedParkingSpots();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchSavedParkingSpots() {
        dbHandler.getSavedParkingSpots(new DatabaseHandler.SavedParkingSpotsCallback() {
            @Override
            public void onSuccess(List<ParkingSpot> spots) {
                if (spots.isEmpty()) {
                    // Show empty state if needed`
                    return;
                }

                // When spots are fetched, reset allParkingSpots
                allParkingSpots.clear();

                // Check for location permission before accessing location
                if (ActivityCompat.checkSelfPermission(SavedSpotsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Update UI without distance data
                    parkingSpots.clear();
                    parkingSpots.addAll(spots);
                    adapter.notifyDataSetChanged();
                    return;
                }

                // Get real-time user location directly
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        // Geocode addresses and calculate distances
                        Geocoder geocoder = new Geocoder(SavedSpotsActivity.this, Locale.getDefault());

                        for (ParkingSpot spot : spots) {
                            try {
                                // Geocode the address to get coordinates
                                List<Address> addresses = geocoder.getFromLocationName(spot.getAddress(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address spotAddress = addresses.get(0);
                                    double spotLat = spotAddress.getLatitude();
                                    double spotLng = spotAddress.getLongitude();

                                    // Calculate distance with real-time location
                                    float[] results = new float[1];
                                    Location.distanceBetween(
                                            location.getLatitude(), location.getLongitude(),
                                            spotLat, spotLng, results);

                                    // Store distance in kilometers
                                    spot.setDistance(results[0] / 1000);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // Update the list with or without distance data
                    parkingSpots.clear();
                    parkingSpots.addAll(spots);
                    adapter.notifyDataSetChanged();
                });

                // Store a copy of all spots for filtering
                allParkingSpots.addAll(spots);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SavedSpotsActivity.this, "Failed to load saved spots: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterParkingSpots(String query) {
        if (allParkingSpots.isEmpty()) {
            // First search, save all spots
            allParkingSpots.addAll(parkingSpots);
        }

        // Clear current displayed list
        parkingSpots.clear();

        if (query.isEmpty()) {
            // If query is empty, show all spots
            parkingSpots.addAll(allParkingSpots);
        } else {
            // Filter by title or address containing the query (case insensitive)
            String lowerCaseQuery = query.toLowerCase();
            for (ParkingSpot spot : allParkingSpots) {
                if (spot.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        spot.getAddress().toLowerCase().contains(lowerCaseQuery)) {
                    parkingSpots.add(spot);
                }
            }
        }

        // Update the adapter
        adapter.notifyDataSetChanged();
    }
}