package com.reserve.app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.List;

public class AddLocationActivity extends AppCompatActivity {
    TextView tvHeader, tvExplore, tvSaved, tvUpdates, tvAdd;
    CardView addLocationContainer;
    LinearLayout navExplore, navSaved, navUpdates, navAdd;
    ShapeableImageView ivProfile;

    // Locations
    Dialog currentLocationDialog;
    String selectedLocationAddress = "";
    private static final int MAP_LOCATION_REQUEST_CODE = 100;

    // Parking Spots
    private List<ParkingSpot> parkingSpots = new ArrayList<>();
    private String[] spotIds;
    private OwnerParkingSpotsAdapter adapter;
    private RecyclerView parkingList;
    private FirebaseFirestore db;

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
        addLocationContainer = findViewById(R.id.addLocationContainer);
        parkingList = findViewById(R.id.parking_list);

        // Access Database
        db = FirebaseFirestore.getInstance();

        // Initialize adapter
        parkingList.setLayoutManager(new LinearLayoutManager(this));

        // Set up adapter
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

        loadUserParkingSpots();

        addLocationContainer.setOnClickListener(v -> {
            showAddLocationForm();
        });

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

    private void showAddLocationForm() {
        // Create a dialog with a custom layout
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

        // Set up map selection
        btnPickLocation.setOnClickListener(v -> {
            // Launch map picker
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

            // Save to database
            DatabaseHandler db = DatabaseHandler.getInstance(this);
            db.createParkingSpace(
                    etLocationName.getText().toString(),
                    selectedLocationAddress,
                    Double.parseDouble(etRate3h.getText().toString()),
                    Double.parseDouble(etRate6h.getText().toString()),
                    Double.parseDouble(etRate12h.getText().toString()),
                    Double.parseDouble(etRate24h.getText().toString()),
                    new DatabaseHandler.BooleanCallback() {
                        @Override
                        public void onResult(boolean result) {
                            Toast.makeText(AddLocationActivity.this, "Location added successfully", Toast.LENGTH_SHORT).show();
                            currentLocationDialog.dismiss();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(AddLocationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        currentLocationDialog.show();
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

    private void loadUserParkingSpots() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Show loading indicator if needed
        // progressBar.setVisibility(View.VISIBLE);

        db.collection("parking_spaces")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ParkingSpot> newSpots = new ArrayList<>();
                    String[] newSpotIds = new String[queryDocumentSnapshots.size()];

                    int i = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString("name");
                        String location = doc.getString("location");
                        Double rate3h = doc.getDouble("rate3h");
                        Double rate6h = doc.getDouble("rate6h");
                        Double rate12h = doc.getDouble("rate12h");
                        Double rate24h = doc.getDouble("rate24h");

                        String price3Hours = "₱" + String.format("%.2f", rate3h);
                        String price6Hours = "₱" + String.format("%.2f", rate6h);
                        String price12Hours = "₱" + String.format("%.2f", rate12h);
                        String pricePerDay = "₱" + String.format("%.2f", rate24h);

                        newSpots.add(new ParkingSpot(name, location,
                                R.drawable.ic_map_placeholder, price3Hours,
                                price6Hours, price12Hours, pricePerDay));

                        newSpotIds[i++] = doc.getId();
                    }

                    // Update existing lists instead of recreating them
                    parkingSpots.clear();
                    parkingSpots.addAll(newSpots);
                    spotIds = newSpotIds;

                    // Only create adapter if it doesn't exist
                    if (adapter == null) {
                        adapter = new OwnerParkingSpotsAdapter(AddLocationActivity.this,
                                parkingSpots, spotIds,
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
                    } else {
                        // Update adapter's data
                        adapter.updateData(parkingSpots, spotIds);
                        adapter.notifyDataSetChanged();
                    }

                    // Hide loading indicator if needed
                    // progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator if needed
                    // progressBar.setVisibility(View.GONE);

                    Toast.makeText(AddLocationActivity.this, "Failed to load parking spots: " +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void editParkingSpot(int position, String spotId) {
        db.collection("parking_spaces").document(spotId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Create dialog for editing
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
                    etName.setText(documentSnapshot.getString("name"));
                    selectedLocationAddress = documentSnapshot.getString("location");
                    tvAddress.setText(selectedLocationAddress);

                    Double rate3h = documentSnapshot.getDouble("rate3h");
                    Double rate6h = documentSnapshot.getDouble("rate6h");
                    Double rate12h = documentSnapshot.getDouble("rate12h");
                    Double rate24h = documentSnapshot.getDouble("rate24h");

                    etRate3h.setText(rate3h.toString());
                    etRate6h.setText(rate6h.toString());
                    etRate12h.setText(rate12h.toString());
                    etRate24h.setText(rate24h.toString());

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

                        // Update parking spot in Firestore
                        db.collection("parking_spaces").document(spotId)
                                .update(
                                        "name", etName.getText().toString(),
                                        "location", selectedLocationAddress,
                                        "rate3h", Double.parseDouble(etRate3h.getText().toString()),
                                        "rate6h", Double.parseDouble(etRate6h.getText().toString()),
                                        "rate12h", Double.parseDouble(etRate12h.getText().toString()),
                                        "rate24h", Double.parseDouble(etRate24h.getText().toString())
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AddLocationActivity.this, "Parking spot updated",
                                            Toast.LENGTH_SHORT).show();
                                    loadUserParkingSpots();  // Refresh the list
                                    editDialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddLocationActivity.this,
                                            "Error updating parking spot: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    });

                    editDialog.show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading parking spot data: " +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
        db.collection("parking_spaces").document(spotId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Parking spot deleted",
                            Toast.LENGTH_SHORT).show();

                    // Update local data
                    parkingSpots.remove(position);

                    // Create new spotIds array without the deleted item
                    String[] newSpotIds = new String[spotIds.length - 1];
                    int index = 0;
                    for (int i = 0; i < spotIds.length; i++) {
                        if (i != position) {
                            newSpotIds[index++] = spotIds[i];
                        }
                    }
                    spotIds = newSpotIds;

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting parking spot: " +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning to this activity
        loadUserParkingSpots();
    }
}