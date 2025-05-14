package com.reserve.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ParkingSpotAdapter extends RecyclerView.Adapter<ParkingSpotAdapter.ViewHolder> {
    private Context context;
    private List<ParkingSpot> spotList;
    private List<HomepageActivity.ParkingSpotWithDistance> spotsWithDistance;
    private DatabaseHandler dbHandler;

    public ParkingSpotAdapter(Context context, List<ParkingSpot> spotList) {
        this.context = context;
        this.spotList = spotList;
    }

    public void setDistanceData(Context context, List<HomepageActivity.ParkingSpotWithDistance> spotsWithDistance) {
        this.spotsWithDistance = spotsWithDistance;
        notifyDataSetChanged();
    }

    public void updateSpots(List<ParkingSpot> spots) {
        this.spotList = spots;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingSpot spot = spotList.get(position);
        holder.title.setText(spot.title);
        holder.address.setText(spot.address);
        holder.image.setImageResource(spot.imageResId);
        holder.hour3Rate.setText(spot.price3Hours);
        holder.hour6Rate.setText(spot.price6Hours);
        holder.hour12Rate.setText(spot.price12Hours);
        holder.perDayRate.setText(spot.pricePerDay);

        // Initialize database handler if needed
        if (dbHandler == null) {
            dbHandler = DatabaseHandler.getInstance(context);
        }

        // Check if this spot is saved by the user and update icon color
        checkIfSpotIsSaved(spot.id, isSaved -> {
            if (isSaved) {
                holder.saveBtn.setColorFilter(Color.parseColor("#FFC107")); // Yellow color
            } else {
                holder.saveBtn.setColorFilter(Color.parseColor("#757575")); // Black color
            }
        });

        // Display distance if available
        if (spotsWithDistance != null) {
            // Find the matching spot with distance
            for (HomepageActivity.ParkingSpotWithDistance spd : spotsWithDistance) {
                if (spd.spot.id.equals(spot.id)) {
                    // Format distance to 1 decimal place
                    String formattedDistance = String.format("%.2f km away", spd.distance);
                    holder.distanceText.setText(formattedDistance);
                    holder.distanceText.setVisibility(View.VISIBLE);
                    break;
                }
            }
        } else {
            holder.distanceText.setText("Distance Unavailable");
        }

        // Set save button click listener
        holder.saveBtn.setOnClickListener(v -> {
            dbHandler.saveParkingSpot(spot, new DatabaseHandler.BooleanCallback() {
                @Override
                public void onResult(boolean result) {
                    // Update the bookmark icon color based on the save action result
                    if (result) {
                        // Spot was saved
                        holder.saveBtn.setColorFilter(Color.parseColor("#FFC107")); // Yellow color
                        Toast.makeText(context, "Added to saved spots", Toast.LENGTH_SHORT).show();
                    } else {
                        // Spot was removed
                        holder.saveBtn.setColorFilter(Color.parseColor("#757575")); // Black color
                        Toast.makeText(context, "Removed from saved spots", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Handle the book button click event
        holder.bookButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookingActivity.class);
            intent.putExtra("SPOT_ID", spot.id);
            intent.putExtra("SPOT_NAME", spot.getTitle());
            intent.putExtra("SPOT_LOCATION", spot.getAddress());

            // Pass pricing information
            intent.putExtra("PRICE_3H", spot.price3Hours);
            intent.putExtra("PRICE_6H", spot.price6Hours);
            intent.putExtra("PRICE_12H", spot.price12Hours);
            intent.putExtra("PRICE_24H", spot.pricePerDay);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return spotList.size();
    }

    private void checkIfSpotIsSaved(String spotId, SavedCheckCallback callback) {
        if (dbHandler == null) {
            dbHandler = DatabaseHandler.getInstance(context);
        }

        dbHandler.checkIfSpotIsSaved(spotId, new DatabaseHandler.BooleanCallback() {
            @Override
            public void onResult(boolean isSaved) {
                callback.onResult(isSaved);
            }

            @Override
            public void onError(Exception e) {
                callback.onResult(false);
            }
        });
    }

    // Interface for callback from saved check
    private interface SavedCheckCallback {
        void onResult(boolean isSaved);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, address, distanceText, hour3Rate, hour6Rate, hour12Rate, perDayRate;
        ImageView image;
        ImageButton saveBtn;
        Button bookButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.location_title);
            address = itemView.findViewById(R.id.location_address);
            image = itemView.findViewById(R.id.map_thumbnail);
            hour3Rate = itemView.findViewById(R.id.et_rate_3h);
            hour6Rate = itemView.findViewById(R.id.et_rate_6h);
            hour12Rate = itemView.findViewById(R.id.et_rate_12h);
            perDayRate = itemView.findViewById(R.id.per_day);
            saveBtn = itemView.findViewById(R.id.saveBtn);
            distanceText = itemView.findViewById(R.id.tv_distance);
            bookButton = itemView.findViewById(R.id.book_button);
        }
    }
}