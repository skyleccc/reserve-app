package com.reserve.app;

        import android.content.Intent;
        import android.os.Bundle;
        import android.widget.LinearLayout;
        import android.widget.Toast;

        import androidx.activity.EdgeToEdge;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.graphics.Insets;
        import androidx.core.view.ViewCompat;
        import androidx.core.view.WindowInsetsCompat;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.google.android.material.imageview.ShapeableImageView;

        import java.util.ArrayList;
        import java.util.List;

        public class SavedSpots extends AppCompatActivity {
            RecyclerView savedList;
            private List<ParkingSpot> parkingSpots = new ArrayList<>();
            private ParkingSpotAdapter adapter;
            private DatabaseHandler dbHandler;

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

                // Initialize navigation buttons
                setupNavigation();

                // Initialize RecyclerView and adapter
                savedList = findViewById(R.id.saved_list);
                savedList.setLayoutManager(new LinearLayoutManager(this));
                adapter = new ParkingSpotAdapter(this, parkingSpots);
                savedList.setAdapter(adapter);

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
                navUpdates.setOnClickListener(v -> {
                    // Navigate to updates
                });
                navAdd.setOnClickListener(v -> startActivity(new Intent(this, AddLocationActivity.class)));
                navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
            }

            private void fetchSavedParkingSpots() {
                dbHandler.getSavedParkingSpots(new DatabaseHandler.SavedParkingSpotsCallback() {
                    @Override
                    public void onSuccess(List<ParkingSpot> spots) {
                        parkingSpots.clear();
                        parkingSpots.addAll(spots);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(SavedSpots.this, "Failed to load saved spots: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }