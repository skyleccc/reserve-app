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
            // TODO: Handle booking
        });
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
            perDayRate  = itemView.findViewById(R.id.per_day);
            bookButton = itemView.findViewById(R.id.book_button);
        }
    }

    public void updateList(List<ParkingSpot> newList){
        spotList.clear();
        spotList.addAll(newList);
        notifyDataSetChanged();
    }
}