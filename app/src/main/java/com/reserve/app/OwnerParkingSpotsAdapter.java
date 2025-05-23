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

public class OwnerParkingSpotsAdapter extends RecyclerView.Adapter<OwnerParkingSpotsAdapter.ViewHolder> {
    private Context context;
    private List<ParkingSpot> spotList;
    private String[] spotIds;
    private OnParkingSpotActionListener listener;

    public interface OnParkingSpotActionListener {
        void onEditClick(int position, String spotId);
        void onDeleteClick(int position, String spotId);
    }

    public OwnerParkingSpotsAdapter(Context context, List<ParkingSpot> spotList,
                                    String[] spotIds, OnParkingSpotActionListener listener) {
        this.context = context;
        this.spotList = spotList;
        this.spotIds = spotIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_parking_owner_card, parent, false);
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

        // IMPORTANT: Don't store position in a final variable
        // Use holder.getBindingAdapterPosition() at click time instead
        holder.editButton.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (listener != null && currentPos != RecyclerView.NO_POSITION && currentPos < spotIds.length) {
                listener.onEditClick(currentPos, spotIds[currentPos]);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (listener != null && currentPos != RecyclerView.NO_POSITION && currentPos < spotIds.length) {
                listener.onDeleteClick(currentPos, spotIds[currentPos]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return spotList == null ? 0 : spotList.size();
    }

    public void updateData(List<ParkingSpot> newSpotList, String[] newSpotIds) {
        this.spotList = newSpotList;
        this.spotIds = newSpotIds;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, address, hour3Rate, hour6Rate, hour12Rate, perDayRate;
        ImageView image;
        Button editButton, deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.location_title);
            address = itemView.findViewById(R.id.location_address);
            image = itemView.findViewById(R.id.map_thumbnail);
            hour3Rate = itemView.findViewById(R.id.et_rate_3h);
            hour6Rate = itemView.findViewById(R.id.et_rate_6h);
            hour12Rate = itemView.findViewById(R.id.et_rate_12h);
            perDayRate = itemView.findViewById(R.id.per_day);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}