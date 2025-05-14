package com.reserve.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavedSpotsAdapter extends RecyclerView.Adapter<SavedSpotsAdapter.ViewHolder> {
    private final Context context;
    private final List<ParkingSpot> savedSpots;

    public SavedSpotsAdapter(Context context, List<ParkingSpot> savedSpots) {
        this.context = context;
        this.savedSpots = savedSpots;
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

        holder.removeButton.setOnClickListener(v -> {
            savedSpots.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, savedSpots.size());
            Toast.makeText(context, spot.getTitle() + " removed from saved spots.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return savedSpots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, address;
        ImageButton removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.location_title); // Replace with actual ID
            address = itemView.findViewById(R.id.location_address); // Replace with actual ID
//            removeButton = itemView.findViewById(R.id.btn_remove); // Replace with actual ID
        }
    }
}