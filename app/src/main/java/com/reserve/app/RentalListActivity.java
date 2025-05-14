package com.reserve.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RentalListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RentalAdapter adapter;
    private Spinner spinnerRentalType;
    private FirebaseFirestore db;
    private List<Rental> rentalsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rental_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Bottom nav click listeners
        LinearLayout navExplore = findViewById(R.id.nav_explore);
        LinearLayout navSaved = findViewById(R.id.nav_saved);
        LinearLayout navUpdates = findViewById(R.id.nav_updates);
        LinearLayout navAdd = findViewById(R.id.nav_add);
        ShapeableImageView navProfile = findViewById(R.id.iv_profile);

        navExplore.setOnClickListener(v -> startActivity(new Intent(this, HomepageActivity.class)));
        navSaved.setOnClickListener(v -> startActivity(new Intent(this, SavedSpotsActivity.class)));
        navUpdates.setOnClickListener(v -> startActivity(new Intent(this, RentalListActivity.class)));
        navAdd.setOnClickListener(v -> startActivity(new Intent(this, AddLocationActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.rentals_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RentalAdapter(this, rentalsList);
        recyclerView.setAdapter(adapter);

        // Initialize spinner
        spinnerRentalType = findViewById(R.id.spinner_rental_type);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.rental_types, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRentalType.setAdapter(spinnerAdapter);

        spinnerRentalType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // position 0: Current Rentals, position 1: Past Rentals
                adapter.setShowCurrentRentals(position == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Default to show current rentals
                adapter.setShowCurrentRentals(true);
            }
        });

        // Load rentals
        loadRentals();
    }

    private void loadRentals() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        db.collection("rentals")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Rental> tempList = new ArrayList<>();
                        List<DocumentSnapshot> rentalDocs = task.getResult().getDocuments();

                        if (rentalDocs.isEmpty()) {
                            // Update adapter with empty list
                            adapter.setRentals(tempList);
                            return;
                        }

                        // Process each rental document by looking up its parking space
                        for (DocumentSnapshot rentalDoc : rentalDocs) {
                            String parkingSpaceId = rentalDoc.getString("parkingSpaceId");
                            if (parkingSpaceId != null) {
                                String id = rentalDoc.getId();
                                String startTime = rentalDoc.getString("startTime");
                                String endTime = rentalDoc.getString("endTime");
                                Double totalCost = rentalDoc.getDouble("totalCost");
                                String status = rentalDoc.getString("status");
                                String licensePlate = rentalDoc.getString("licensePlate");
                                String vehicleDescription = rentalDoc.getString("vehicleDescription");

                                // Look up parking space details
                                db.collection("parking_spaces").document(parkingSpaceId).get()
                                        .addOnSuccessListener(parkingDoc -> {
                                            if (parkingDoc.exists()) {
                                                String spotName = parkingDoc.getString("name");
                                                String spotLocation = parkingDoc.getString("location");

                                                Rental rental = new Rental(
                                                        id,
                                                        parkingSpaceId,
                                                        spotName,
                                                        spotLocation,
                                                        startTime,
                                                        endTime,
                                                        totalCost != null ? totalCost : 0.0,
                                                        status,
                                                        licensePlate,
                                                        vehicleDescription
                                                );

                                                tempList.add(rental);

                                                // Update adapter when all rentals are processed
                                                if (tempList.size() == rentalDocs.size()) {
                                                    rentalsList.clear();
                                                    rentalsList.addAll(tempList);
                                                    adapter.setRentals(rentalsList);
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }
}