package com.reserve.app;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AddLocationActivity extends AppCompatActivity {
    TextView tvHeader, tvExplore, tvSaved, tvUpdates, tvAdd;
    CardView addLocationContainer;
    LinearLayout navExplore, navSaved, navUpdates, navAdd;
    ShapeableImageView ivProfile;
    ProgressBar progressBar;

    // Locations
    Dialog currentLocationDialog;
    String selectedLocationAddress = "";
    private static final int MAP_LOCATION_REQUEST_CODE = 100;

    // Parking Spots
    private boolean isDataInitialized = false;
    private List<ParkingSpot> allParkingSpots = new ArrayList<>();
    private List<ParkingSpot> parkingSpots = new ArrayList<>();
    private String[] allSpotIds;
    private String[] spotIds;
    private OwnerParkingSpotsAdapter adapter;
    private RecyclerView parkingList;
    private DatabaseHandler dbHandler;

    // Optimization 1: Geocoder cache and executor service
    private Map<String, Address> geocodeCache = new HashMap<>();
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    // Optimization 5: Cache for Firestore data
    private List<ParkingSpot> parkingSpotCache;
    private String[] spotIdsCache;

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
        addLocationContainer = findViewById(R.id.addLocationContainer);
        parkingList = findViewById(R.id.parking_list);
        progressBar = findViewById(R.id.progress_bar);

        // Bottom nav click listeners
        navExplore = findViewById(R.id.nav_explore);
        navSaved = findViewById(R.id.nav_saved);
        navUpdates = findViewById(R.id.nav_updates);
        navAdd = findViewById(R.id.nav_add);

        // Access Database
        dbHandler = DatabaseHandler.getInstance(this);

        // Initialize adapter and RecyclerView
        parkingList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OwnerParkingSpotsAdapter(this, parkingSpots, new String[0],
                new OwnerParkingSpotsAdapter.OnParkingSpotActionListener() {
                    @Override
                    public void onEditClick(int position, String spotId) {
                        editParkingSpot(position, spotId);
                    }

                    @Override
                    public void onDeleteClick(int position, String spotId) {
                        confirmDeleteParkingSpot(position, spotId);
                    }
                });
        parkingList.setAdapter(adapter);

        // Optimization 5: Load data with caching
        loadUserParkingSpots(true);

        // Setup click listeners
        setupClickListeners();

        // Setup search functionality
        setupSearchBar();

        // Optimization 4: Initialize location tracking if needed
        initLocationTracking();
    }

    private void setupClickListeners() {
        addLocationContainer.setOnClickListener(v -> showAddLocationForm());

        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        navExplore.setOnClickListener(v -> startActivity(new Intent(this, HomepageActivity.class)));

        navSaved.setOnClickListener(v -> startActivity(new Intent(this, HomepageActivity.class)));

        navUpdates.setOnClickListener(v -> startActivity(new Intent(this, HomepageActivity.class)));
    }

    private void setupSearchBar() {
        EditText searchEditText = findViewById(R.id.et_search_spots);
        LinearLayout searchBar = findViewById(R.id.search_bar);

        // Add text change listener for real-time filtering
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterParkingSpots(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Make search bar clickable with improved focus handling
        searchBar.setClickable(true);
        searchBar.setFocusable(true);
        searchBar.setOnClickListener(v -> {
            // Set focus to the EditText
            searchEditText.requestFocus();

            // Show the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    private void filterParkingSpots(String query) {
        // Handle the case when no data is loaded yet
        if (allParkingSpots == null || allParkingSpots.isEmpty() || allSpotIds == null) {
            return;
        }

        List<ParkingSpot> filteredSpots = new ArrayList<>();
        List<String> filteredIds = new ArrayList<>();

        if (query.isEmpty()) {
            // If search is empty, use all spots
            filteredSpots.addAll(allParkingSpots);
            for (String id : allSpotIds) {
                filteredIds.add(id);
            }
        } else {
            // Convert to lowercase for case-insensitive search
            final String lowercaseQuery = query.toLowerCase();

            // Filter spots by title or address
            for (int i = 0; i < allParkingSpots.size(); i++) {
                ParkingSpot spot = allParkingSpots.get(i);
                if ((spot.title != null && spot.title.toLowerCase().contains(lowercaseQuery)) ||
                        (spot.address != null && spot.address.toLowerCase().contains(lowercaseQuery))) {
                    filteredSpots.add(spot);
                    // Make sure we don't go out of bounds with allSpotIds
                    if (i < allSpotIds.length) {
                        filteredIds.add(allSpotIds[i]);
                    }
                }
            }
        }

        // Update the activity's lists to match the filtered data
        parkingSpots.clear();
        parkingSpots.addAll(filteredSpots);
        spotIds = filteredIds.toArray(new String[0]);

        // Update adapter with the same data
        adapter.updateData(parkingSpots, spotIds);
    }

    // Optimization 4: Initialize location tracking
    private void initLocationTracking() {
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.getUserLat() == 0 && sessionManager.getUserLng() == 0) {
            ReserveApplication app = (ReserveApplication) getApplicationContext();
            app.startLocationTracking(location -> {
                sessionManager.saveUserLocation(location.getLatitude(), location.getLongitude());

                // Save to Firebase if user is logged in
                String userId = sessionManager.getUserId();
                if (userId != null) {
                    dbHandler.updateUserLocation(userId, location.getLatitude(), location.getLongitude(),
                            new DatabaseHandler.BooleanCallback() {
                                @Override
                                public void onResult(boolean result) {
                                    // Success handled silently
                                }

                                @Override
                                public void onError(Exception e) {
                                    // Optional: you could log the error or show a toast
                                }
                            });
                }

                // Stop updates after getting location once
                app.getLocationTracker().stopLocationUpdates();
            });
        }
    }

    private void showAddLocationForm() {
        currentLocationDialog = new Dialog(this);
        currentLocationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        currentLocationDialog.setContentView(R.layout.dialog_add_location);

        // Make dialog full width
        Window window = currentLocationDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Get references to form fields
        EditText etLocationName = currentLocationDialog.findViewById(R.id.et_location_name);
        EditText etRate3h = currentLocationDialog.findViewById(R.id.et_rate_3h);
        EditText etRate6h = currentLocationDialog.findViewById(R.id.et_rate_6h);
        EditText etRate12h = currentLocationDialog.findViewById(R.id.et_rate_12h);
        EditText etRate24h = currentLocationDialog.findViewById(R.id.et_rate_24h);
        TextView tvLocationAddress = currentLocationDialog.findViewById(R.id.tv_location_address);
        Button btnPickLocation = currentLocationDialog.findViewById(R.id.btn_pick_location);
        Button btnSave = currentLocationDialog.findViewById(R.id.btn_save);

        // Added Close Button to add location forms
        Button btnClose = currentLocationDialog.findViewById(R.id.btn_close); // Assuming you've assigned the ID for the "X" button
        btnClose.setOnClickListener(v -> {
            currentLocationDialog.dismiss(); // Close the dialog
        });

        // Set up map selection
        btnPickLocation.setOnClickListener(v -> {
            Intent mapIntent = new Intent(AddLocationActivity.this, MapPickerActivity.class);
            startActivityForResult(mapIntent, MAP_LOCATION_REQUEST_CODE);
        });

        // Set up save button
        btnSave.setOnClickListener(v -> {
            // Validate inputs
            if (etLocationName.getText().toString().isEmpty() ||
                    etRate3h.getText().toString().isEmpty() ||
                    etRate6h.getText().toString().isEmpty() ||
                    etRate12h.getText().toString().isEmpty() ||
                    etRate24h.getText().toString().isEmpty() ||
                    selectedLocationAddress.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading indicator
            progressBar.setVisibility(View.VISIBLE);

            // Optimization 1: Geocode the address in background
            if (!geocodeCache.containsKey(selectedLocationAddress.toLowerCase())) {
                executor.execute(() -> {
                    geocodeAddress(selectedLocationAddress);
                });
            }

            dbHandler.createParkingSpace(
                    etLocationName.getText().toString(),
                    selectedLocationAddress,
                    Double.parseDouble(etRate3h.getText().toString()),
                    Double.parseDouble(etRate6h.getText().toString()),
                    Double.parseDouble(etRate12h.getText().toString()),
                    Double.parseDouble(etRate24h.getText().toString()),
                    new DatabaseHandler.BooleanCallback() {
                        @Override
                        public void onResult(boolean result) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(AddLocationActivity.this, "Location added successfully", Toast.LENGTH_SHORT).show();
                                currentLocationDialog.dismiss();

                                // Refresh the list
                                loadUserParkingSpots(false);
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(AddLocationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        });

        currentLocationDialog.show();
    }

    // Optimization 1: Geocode address in background and cache result
    private void geocodeAddress(String address) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                geocodeCache.put(address.toLowerCase(), addresses.get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String address = data.getStringExtra("address");
            selectedLocationAddress = address;

            // Update the dialog with the selected address if dialog is showing
            if (currentLocationDialog != null && currentLocationDialog.isShowing()) {
                TextView tvLocationAddress = currentLocationDialog.findViewById(R.id.tv_location_address);
                if (tvLocationAddress != null) {
                    tvLocationAddress.setText(address);
                }
            }
        }
    }

    // Optimization 5: Load user parking spots with caching
    private void loadUserParkingSpots(boolean forceRefresh) {
        // Show loading indicator if needed
        progressBar.setVisibility(View.VISIBLE);

        // Use cache if available and not forcing refresh
        if (!forceRefresh && parkingSpotCache != null && parkingSpotCache.size() > 0) {
            parkingSpots.clear();
            parkingSpots.addAll(parkingSpotCache);
            spotIds = spotIdsCache.clone();

            // Store complete data for filtering
            allParkingSpots = new ArrayList<>(parkingSpotCache);
            allSpotIds = spotIdsCache.clone();

            adapter.updateData(parkingSpots, spotIds);
            progressBar.setVisibility(View.GONE);

            // Refresh in background
            refreshParkingSpotsInBackground();
            return;
        }

        dbHandler.getUserParkingSpots(forceRefresh, new DatabaseHandler.ParkingSpotsCallback() {
            @Override
            public void onSuccess(List<ParkingSpot> newSpots, String[] newSpotIds) {
                // Update cache
                parkingSpotCache = new ArrayList<>(newSpots);
                spotIdsCache = newSpotIds.clone();

                // Store complete data for filtering
                allParkingSpots = new ArrayList<>(newSpots);
                allSpotIds = newSpotIds.clone();

                // Update existing lists
                parkingSpots.clear();
                parkingSpots.addAll(newSpots);
                spotIds = newSpotIds;

                // Update UI with empty view if no spots
                TextView emptyView = findViewById(R.id.empty_view);
                if (newSpots.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    parkingList.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    parkingList.setVisibility(View.VISIBLE);
                }

                adapter.updateData(parkingSpots, spotIds);
                progressBar.setVisibility(View.GONE);

                // Mark data as initialized
                isDataInitialized = true;
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddLocationActivity.this, "Failed to load parking spots: " +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Optimization 5: Refresh parking spots in background
    private void refreshParkingSpotsInBackground() {
        dbHandler.getUserParkingSpots(true, new DatabaseHandler.ParkingSpotsCallback() {
            @Override
            public void onSuccess(List<ParkingSpot> newSpots, String[] newSpotIds) {
                // Update cache
                parkingSpotCache = new ArrayList<>(newSpots);
                spotIdsCache = newSpotIds.clone();

                // If data has changed, update UI
                if (spotIds.length != newSpotIds.length || !parkingSpots.equals(newSpots)) {
                    runOnUiThread(() -> {
                        parkingSpots.clear();
                        parkingSpots.addAll(newSpots);
                        spotIds = newSpotIds;
                        adapter.updateData(parkingSpots, spotIds);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Silent failure in background refresh
            }
        });
    }

    private void editParkingSpot(int position, String spotId) {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);

        // Optimization 5: Check cache first
        if (position < parkingSpots.size()) {
            setupEditDialog(spotId, position);
            return;
        }

        dbHandler.getParkingSpotById(spotId, new DatabaseHandler.ParkingSpotCallback() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                progressBar.setVisibility(View.GONE);
                setupEditDialog(spotId, documentSnapshot);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddLocationActivity.this, "Error loading parking spot data: " +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupEditDialog(String spotId, int position) {
        ParkingSpot spot = parkingSpots.get(position);

        // Create dialog with prepopulated fields
        Dialog editDialog = createEditDialog(
                spot.title,
                spot.address,
                spot.price3Hours.replace("₱", ""),
                spot.price6Hours.replace("₱", ""),
                spot.price12Hours.replace("₱", ""),
                spot.pricePerDay.replace("₱", ""),
                spotId
        );

        editDialog.show();
        progressBar.setVisibility(View.GONE);
    }

    private void setupEditDialog(String spotId, DocumentSnapshot documentSnapshot) {
        String name = documentSnapshot.getString("name");
        String location = documentSnapshot.getString("location");
        Double rate3h = documentSnapshot.getDouble("rate3h");
        Double rate6h = documentSnapshot.getDouble("rate6h");
        Double rate12h = documentSnapshot.getDouble("rate12h");
        Double rate24h = documentSnapshot.getDouble("rate24h");

        // Create dialog with prepopulated fields
        Dialog editDialog = createEditDialog(
                name,
                location,
                rate3h.toString(),
                rate6h.toString(),
                rate12h.toString(),
                rate24h.toString(),
                spotId
        );

        editDialog.show();
    }

    private Dialog createEditDialog(String name, String location, String rate3h,
                                    String rate6h, String rate12h, String rate24h,
                                    String spotId) {
        Dialog editDialog = new Dialog(this);
        editDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editDialog.setContentView(R.layout.dialog_add_location);

        Window window = editDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Set dialog title
        TextView dialogTitle = editDialog.findViewById(R.id.dialog_title);
        if (dialogTitle != null) {
            dialogTitle.setText("Edit Parking Location");
        }

        // Initialize dialog fields
        EditText etName = editDialog.findViewById(R.id.et_location_name);
        EditText etRate3h = editDialog.findViewById(R.id.et_rate_3h);
        EditText etRate6h = editDialog.findViewById(R.id.et_rate_6h);
        EditText etRate12h = editDialog.findViewById(R.id.et_rate_12h);
        EditText etRate24h = editDialog.findViewById(R.id.et_rate_24h);
        TextView tvAddress = editDialog.findViewById(R.id.tv_location_address);
        Button btnPickLocation = editDialog.findViewById(R.id.btn_pick_location);
        Button btnSave = editDialog.findViewById(R.id.btn_save);

        // Populate fields with existing data
        etName.setText(name);
        selectedLocationAddress = location;
        tvAddress.setText(selectedLocationAddress);

        etRate3h.setText(rate3h);
        etRate6h.setText(rate6h);
        etRate12h.setText(rate12h);
        etRate24h.setText(rate24h);

        btnSave.setText("Update Location");

        // Set up map selection
        btnPickLocation.setOnClickListener(v -> {
            currentLocationDialog = editDialog;
            Intent mapIntent = new Intent(AddLocationActivity.this, MapPickerActivity.class);
            startActivityForResult(mapIntent, MAP_LOCATION_REQUEST_CODE);
        });

        // Save button click listener
        btnSave.setOnClickListener(v -> {
            // Validate inputs
            if (etName.getText().toString().isEmpty() ||
                    etRate3h.getText().toString().isEmpty() ||
                    etRate6h.getText().toString().isEmpty() ||
                    etRate12h.getText().toString().isEmpty() ||
                    etRate24h.getText().toString().isEmpty() ||
                    selectedLocationAddress.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Optimization 1: Geocode in background if needed
            if (!geocodeCache.containsKey(selectedLocationAddress.toLowerCase())) {
                executor.execute(() -> {
                    geocodeAddress(selectedLocationAddress);
                });
            }

            // Update parking spot using DatabaseHandler
            dbHandler.updateParkingSpot(
                    spotId,
                    etName.getText().toString(),
                    selectedLocationAddress,
                    Double.parseDouble(etRate3h.getText().toString()),
                    Double.parseDouble(etRate6h.getText().toString()),
                    Double.parseDouble(etRate12h.getText().toString()),
                    Double.parseDouble(etRate24h.getText().toString()),
                    new DatabaseHandler.BooleanCallback() {
                        @Override
                        public void onResult(boolean result) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddLocationActivity.this, "Parking spot updated",
                                    Toast.LENGTH_SHORT).show();
                            loadUserParkingSpots(true);  // Force refresh the list
                            editDialog.dismiss();
                        }

                        @Override
                        public void onError(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddLocationActivity.this,
                                    "Error updating parking spot: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        return editDialog;
    }

    private void confirmDeleteParkingSpot(int position, String spotId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Parking Spot")
                .setMessage("Are you sure you want to delete this parking spot?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteParkingSpot(position, spotId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteParkingSpot(int position, String spotId) {
        progressBar.setVisibility(View.VISIBLE);

        dbHandler.deleteParkingSpot(spotId, new DatabaseHandler.BooleanCallback() {
            @Override
            public void onResult(boolean result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddLocationActivity.this, "Parking spot deleted",
                        Toast.LENGTH_SHORT).show();

                // Optimization 3: Update lists more efficiently
                if (position < parkingSpots.size()) {
                    parkingSpots.remove(position);
                }

                // Create new spotIds array without the deleted item
                String[] newSpotIds = new String[spotIds.length - 1];
                int index = 0;
                for (int i = 0; i < spotIds.length; i++) {
                    if (i != position) {
                        newSpotIds[index++] = spotIds[i];
                    }
                }
                spotIds = newSpotIds;

                // Update cache
                parkingSpotCache = new ArrayList<>(parkingSpots);
                spotIdsCache = spotIds.clone();

                adapter.updateData(parkingSpots, spotIds);
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddLocationActivity.this, "Error deleting parking spot: " +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Only load parking spots if we don't have data yet
        // or if we're returning from another activity
        if (!isDataInitialized) {
            loadUserParkingSpots(true);
            isDataInitialized = true;
        } else {
            // Refresh data in background without showing loading indicator
            refreshParkingSpotsInBackground();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop location updates when app is in background to save battery
        ReserveApplication app = (ReserveApplication) getApplicationContext();
        if (app.getLocationTracker() != null) {
            app.getLocationTracker().stopLocationUpdates();
        }
    }
}