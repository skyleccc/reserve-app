package com.reserve.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ParkingSpotAdapter extends RecyclerView.Adapter<ParkingSpotAdapter.ViewHolder> {
    private Context context;
    private List<ParkingSpot> spotList;

    public ParkingSpotAdapter(Context context, List<ParkingSpot> spotList) {
        this.context = context;
        this.spotList = spotList;
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
        holder.hour3Rate.setText("3 Hours = " + spot.price3Hours);
        holder.hour6Rate.setText("6 Hours = " + spot.price6Hours);
        holder.hour12Rate.setText("12 Hours = " + spot.price12Hours);
        holder.perDayRate.setText("1 Day = " + spot.pricePerDay);

        holder.bookButton.setOnClickListener(v -> {
            // Create intent to navigate to BookingActivity
            android.content.Intent intent = new android.content.Intent(context, BookingActivity.class);

            // Ensure we're passing the correct ID
            if (spot.id == null || spot.id.isEmpty()) {
                return; // Skip if no valid ID
            }

            // Add all required extras to the intent
            intent.putExtra("SPOT_ID", spot.id);
            intent.putExtra("SPOT_NAME", spot.title);
            intent.putExtra("SPOT_LOCATION", spot.address);
            intent.putExtra("PRICE_3H", spot.price3Hours);
            intent.putExtra("PRICE_6H", spot.price6Hours);
            intent.putExtra("PRICE_12H", spot.price12Hours);
            intent.putExtra("PRICE_24H", spot.pricePerDay);

            // Start the activity
            context.startActivity(intent);
        });
    }

    public void updateSpots(List<ParkingSpot> newSpots) {
        this.spotList.clear();
        this.spotList.addAll(newSpots);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return spotList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, address, hour3Rate, hour6Rate, hour12Rate, perDayRate;
        ImageView image;
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
            bookButton = itemView.findViewById(R.id.book_button);
        }
    }
}