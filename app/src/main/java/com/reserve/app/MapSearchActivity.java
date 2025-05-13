package com.reserve.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapSearchActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Database
    private DatabaseHandler dbHandler;

    private GoogleMap mMap;
    private EditText searchEditText;
    private Button searchHereButton;
    private FloatingActionButton toggleMarkersButton;
    private Marker selectedMarker;
    private static final float DEFAULT_ZOOM = 15f;
    private List<ParkingMarker> parkingMarkers = new ArrayList<>();
    private boolean markersVisible = true;

    // Add these fields to track the bottom card and its views
    private androidx.cardview.widget.CardView parkingDetailsCard;
    private TextView tvParkingName, tvParkingLocation, tvParkingDistance;
    private TextView tvRate3h, tvRate6h, tvRate12h, tvRate24h;
    private Button btnBookNow;

    // HashMap to link markers to parking spots and their document IDs
    private HashMap<Marker, ParkingSpotInfo> markerInfoMap = new HashMap<>();

    // Helper class to store parking spot with additional info
    private static class ParkingSpotInfo {
        ParkingSpot spot;
        double distance;
        String docId;

        ParkingSpotInfo(ParkingSpot spot, double distance, String docId) {
            this.spot = spot;
            this.distance = distance;
            this.docId = docId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize DatabaseHandler
        dbHandler = DatabaseHandler.getInstance(this);

        // Initialize views
        initializeViews();

        // Setup map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        ImageView backButton = findViewById(R.id.btn_back);
        searchHereButton = findViewById(R.id.btn_search_here);
        toggleMarkersButton = findViewById(R.id.btn_toggle_markers);

        // Initialize details card views
        parkingDetailsCard = findViewById(R.id.parking_details_card);
        tvParkingName = findViewById(R.id.tv_parking_name);
        tvParkingLocation = findViewById(R.id.tv_parking_location);
        tvParkingDistance = findViewById(R.id.tv_parking_distance);
        tvRate3h = findViewById(R.id.tv_rate_3h);
        tvRate6h = findViewById(R.id.tv_rate_6h);
        tvRate12h = findViewById(R.id.tv_rate_12h);
        tvRate24h = findViewById(R.id.tv_rate_24h);
        btnBookNow = findViewById(R.id.btn_book_now);
    }

    private void setupClickListeners() {
        // Setup back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Setup toggle markers button
        toggleMarkersButton.setOnClickListener(v -> toggleMarkers());

        // Setup book now button
        btnBookNow.setOnClickListener(v -> {
            if (selectedMarker != null && markerInfoMap.containsKey(selectedMarker)) {
                ParkingSpotInfo info = markerInfoMap.get(selectedMarker);
                // Start booking activity with spot details
                Intent intent = new Intent(MapSearchActivity.this, BookingActivity.class);
                intent.putExtra("SPOT_NAME", info.spot.title);
                intent.putExtra("SPOT_LOCATION", info.spot.address);
                intent.putExtra("SPOT_ID", info.docId);
                intent.putExtra("PRICE_3H", info.spot.price3Hours);
                intent.putExtra("PRICE_6H", info.spot.price6Hours);
                intent.putExtra("PRICE_12H", info.spot.price12Hours);
                intent.putExtra("PRICE_24H", info.spot.pricePerDay);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Get user's last saved location
        SessionManager sessionManager = new SessionManager(this);
        double userLat = sessionManager.getUserLat();
        double userLng = sessionManager.getUserLng();

        // If we have a valid location, center map there
        if (userLat != 0 && userLng != 0) {
            LatLng userLocation = new LatLng(userLat, userLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM));
        }

        // Enable the blue dot showing user's current location (if permission granted)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            // Use FusedLocationProviderClient to get current location
            FusedLocationProviderClient fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Add marker click listener to show details
        mMap.setOnMarkerClickListener(marker -> {
            selectedMarker = marker;

            if (markerInfoMap.containsKey(marker)) {
                ParkingSpotInfo info = markerInfoMap.get(marker);
                showParkingDetails(info);
            }

            // Return true to prevent default behavior (info window)
            return true;
        });

        // Immediately load all nearby parking spots
        if (userLat != 0 && userLng != 0) {
            searchNearbyParkingSpots(userLat, userLng);
        }
    }

    private void showParkingDetails(ParkingSpotInfo info) {
        // Fill the details card
        tvParkingName.setText(info.spot.title);
        tvParkingLocation.setText(info.spot.address);
        tvParkingDistance.setText(String.format("%.1f km away", info.distance));
        tvRate3h.setText(info.spot.price3Hours);
        tvRate6h.setText(info.spot.price6Hours);
        tvRate12h.setText(info.spot.price12Hours);
        tvRate24h.setText(info.spot.pricePerDay);

        // Show the details card and hide search button
        parkingDetailsCard.setVisibility(View.VISIBLE);
        searchHereButton.setVisibility(View.GONE);
    }

    // To toggle markers visibility
    private void toggleMarkers() {
        markersVisible = !markersVisible;

        // Update all markers visibility
        for (ParkingMarker pm : parkingMarkers) {
            pm.marker.setVisible(markersVisible);
        }

        // Update button icon using system drawables
        toggleMarkersButton.setImageResource(markersVisible ?
                android.R.drawable.ic_menu_view : android.R.drawable.ic_menu_close_clear_cancel);

        // If hiding markers, also hide the parking details card
        if (!markersVisible && parkingDetailsCard.getVisibility() == View.VISIBLE) {
            parkingDetailsCard.setVisibility(View.GONE);
        }
    }

    private void searchNearbyParkingSpots(double latitude, double longitude) {
        // Clear existing markers
        clearParkingMarkers();
        markerInfoMap.clear();

        // Show loading indicator
        Toast.makeText(this, "Searching for parking spots...", Toast.LENGTH_SHORT).show();

        dbHandler.getAllParkingSpotsWithIDs(true, new DatabaseHandler.ParkingSpotWithIDCallback() {
            @Override
            public void onSuccess(List<DocumentSnapshot> documents) {
                for (DocumentSnapshot doc : documents) {
                    String spotId = doc.getId();
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
                    ParkingSpot spot = new ParkingSpot(spotId, name, location,
                            R.drawable.ic_map_placeholder, price3Hours,
                            price6Hours, price12Hours, pricePerDay);

                    // Geocode the parking spot location
                    geocodeAndAddMarker(spotId, spot, latitude, longitude);
                }
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MapSearchActivity.this,
                            "Failed to load parking spots: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void geocodeAndAddMarker(String spotId, ParkingSpot spot, double searchLat, double searchLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(spot.address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                double spotLat = location.getLatitude();
                double spotLng = location.getLongitude();

                // Calculate distance between search location and parking spot
                double distance = calculateDistance(searchLat, searchLng, spotLat, spotLng);

                // Create marker for this parking spot
                LatLng spotLatLng = new LatLng(spotLat, spotLng);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(spotLatLng)
                        .title(spot.title)
                        .snippet(spot.address)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                Marker marker = mMap.addMarker(markerOptions);
                if (marker != null) {
                    // Store marker with its distance for sorting later
                    parkingMarkers.add(new ParkingMarker(marker, distance));

                    // Store detailed information about this marker
                    markerInfoMap.put(marker, new ParkingSpotInfo(spot, distance, spotId));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearParkingMarkers() {
        for (ParkingMarker pm : parkingMarkers) {
            pm.marker.remove();
        }
        parkingMarkers.clear();
    }

    // Class to track markers with their distances
    private static class ParkingMarker {
        Marker marker;
        double distance;

        ParkingMarker(Marker marker, double distance) {
            this.marker = marker;
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