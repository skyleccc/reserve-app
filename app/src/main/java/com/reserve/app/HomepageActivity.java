package com.reserve.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class HomepageActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;

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
        RecyclerView recyclerView = findViewById(R.id.parking_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ParkingSpot> spots = new ArrayList<>();
        spots.add(new ParkingSpot("Near Lechon House", "Azalla's Lechon House, Saint Jude Street, Hippodromo, Cebu City, 6000 Cebu", R.drawable.ic_map_placeholder, "₱30.00", "₱60.00", "₱120.00", "₱345.00"));
        spots.add(new ParkingSpot("SM Seaside Entrance", "SRP-Mambaling Rd, Cebu City, 6000 Cebu", R.drawable.ic_map_placeholder, "₱40.00", "₱80.00", "₱160.00", "₱400.00"));
        spots.add(new ParkingSpot("IT Park Basement", "Geonzon St, Apas, Cebu City, 6000 Cebu", R.drawable.ic_map_placeholder, "₱35.00", "₱70.00", "₱140.00", "₱380.00"));
        spots.add(new ParkingSpot("Near Ayala Center", "Cebu Business Park, Archbishop Reyes Ave, Cebu City", R.drawable.ic_map_placeholder, "₱45.00", "₱90.00", "₱180.00", "₱450.00"));
        spots.add(new ParkingSpot("Robinsons Galleria", "General Maxilom Ave Ext, Cebu City, Cebu", R.drawable.ic_map_placeholder, "₱32.00", "₱64.00", "₱128.00", "₱365.00"));
        spots.add(new ParkingSpot("Cebu South Bus Terminal", "N. Bacalso Ave, Cebu City, Cebu", R.drawable.ic_map_placeholder, "₱28.00", "₱56.00", "₱112.00", "₱310.00"));
        spots.add(new ParkingSpot("Mango Square Lot", "Gen. Maxilom Ave, Cebu City, Cebu", R.drawable.ic_map_placeholder, "₱25.00", "₱50.00", "₱100.00", "₱290.00"));
        spots.add(new ParkingSpot("Parkmall Area", "Ouano Ave, Mandaue City, Cebu", R.drawable.ic_map_placeholder, "₱30.00", "₱60.00", "₱120.00", "₱335.00"));

        ParkingSpotAdapter adapter = new ParkingSpotAdapter(this, spots);
        recyclerView.setAdapter(adapter);

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
        // Refresh location when returning to the app
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            startLocationTracking();
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

            // Stop continuous updates after getting location once
            // Remove this line if you want continuous tracking
            app.getLocationTracker().stopLocationUpdates();
        });
    }
}