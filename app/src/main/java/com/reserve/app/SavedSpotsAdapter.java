package com.reserve.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavedSpotsAdapter extends RecyclerView.Adapter<SavedSpotsAdapter.ViewHolder> {
    private final Context context;
    private final List<ParkingSpot> savedSpots;
    private final DatabaseHandler dbHandler;

    public SavedSpotsAdapter(Context context, List<ParkingSpot> savedSpots) {
        this.context = context;
        this.savedSpots = savedSpots;
        this.dbHandler = DatabaseHandler.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingSpot spot = savedSpots.get(position);
        holder.title.setText(spot.getTitle());
        holder.address.setText(spot.getAddress());
        holder.rate3h.setText(spot.price3Hours);
        holder.rate6h.setText(spot.price6Hours);
        holder.rate12h.setText(spot.price12Hours);
        holder.rate24h.setText(spot.pricePerDay);

        if (spot.getDistance() > 0) {
            String formattedDistance = String.format("%.2f km away", spot.getDistance());
            holder.distance.setText(formattedDistance);
        } else {
            holder.distance.setText("Distance unavailable");
        }
        holder.distance.setVisibility(View.VISIBLE);

        // Set up remove button
        holder.removeButton.setImageResource(R.drawable.ic_bookmark_banner);
        holder.removeButton.setColorFilter(Color.parseColor("#FFC107"));
        holder.removeButton.setOnClickListener(v -> {
            // Remove from saved spots
            dbHandler.removeFromSavedSpots(spot.id, new DatabaseHandler.OperationCallback() {
                @Override
                public void onSuccess() {
                    // Remove from the adapter's list
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        savedSpots.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);

                        Toast.makeText(context, "Removed from saved spots", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context, "Error removing spot: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Set up book button
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
        return savedSpots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, address, distance, rate3h, rate6h, rate12h, rate24h;
        ImageButton removeButton;
        Button bookButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.location_title);
            address = itemView.findViewById(R.id.location_address);
            distance = itemView.findViewById(R.id.tv_distance);
            removeButton = itemView.findViewById(R.id.saveBtn);
            bookButton = itemView.findViewById(R.id.book_button);
            rate3h = itemView.findViewById(R.id.et_rate_3h);
            rate6h = itemView.findViewById(R.id.et_rate_6h);
            rate12h = itemView.findViewById(R.id.et_rate_12h);
            rate24h = itemView.findViewById(R.id.per_day);
        }
    }
}