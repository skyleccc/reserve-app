package com.reserve.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.widget.LinearLayout;

public class HomepageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Parking spots list
        RecyclerView recyclerView = findViewById(R.id.parking_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ParkingSpot> spots = new ArrayList<>();
        spots.add(new ParkingSpot("Near Lechon House", "Azalla's Lechon House, Saint Jude Street, Hippodromo, Cebu City, 6000 Cebu", R.drawable.ic_map_placeholder, "₱30.00", "₱15.00", "₱345.00"));
        spots.add(new ParkingSpot("SM Seaside Entrance", "SRP-Mambaling Rd, Cebu City, 6000 Cebu", R.drawable.ic_map_placeholder, "₱40.00", "₱20.00", "₱400.00"));
        spots.add(new ParkingSpot("IT Park Basement", "Geonzon St, Apas, Cebu City, 6000 Cebu", R.drawable.ic_map_placeholder, "₱35.00", "₱18.00", "₱380.00"));
        spots.add(new ParkingSpot("Near Ayala Center", "Cebu Business Park, Archbishop Reyes Ave, Cebu City", R.drawable.ic_map_placeholder, "₱45.00", "₱25.00", "₱450.00"));
        spots.add(new ParkingSpot("Robinsons Galleria", "General Maxilom Ave Ext, Cebu City, Cebu", R.drawable.ic_map_placeholder, "₱32.00", "₱17.00", "₱365.00"));
        spots.add(new ParkingSpot("Cebu South Bus Terminal", "N. Bacalso Ave, Cebu City, Cebu", R.drawable.ic_map_placeholder, "₱28.00", "₱14.00", "₱310.00"));
        spots.add(new ParkingSpot("Mango Square Lot", "Gen. Maxilom Ave, Cebu City, Cebu", R.drawable.ic_map_placeholder, "₱25.00", "₱12.00", "₱290.00"));
        spots.add(new ParkingSpot("Parkmall Area", "Ouano Ave, Mandaue City, Cebu", R.drawable.ic_map_placeholder, "₱30.00", "₱16.00", "₱335.00"));

        ParkingSpotAdapter adapter = new ParkingSpotAdapter(this, spots);
        recyclerView.setAdapter(adapter);

        // Bottom nav click listeners
        LinearLayout navExplore = findViewById(R.id.nav_explore);
        LinearLayout navSaved = findViewById(R.id.nav_saved);
        LinearLayout navUpdates = findViewById(R.id.nav_updates);
        LinearLayout navAdd = findViewById(R.id.nav_add);

        navExplore.setOnClickListener(v -> {

        });

        navSaved.setOnClickListener(v -> {
            startActivity(new Intent(this, HomepageActivity.class));
        });

        navUpdates.setOnClickListener(v -> {
            startActivity(new Intent(this, HomepageActivity.class));
        });

        navAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddLocationActivity.class));
        });
    }
}