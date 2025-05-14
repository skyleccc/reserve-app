package com.reserve.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RentalAdapter extends RecyclerView.Adapter<RentalAdapter.RentalViewHolder> {

    private List<Rental> allRentals;
    private List<Rental> filteredRentals;
    private Context context;
    private boolean showCurrentRentalsOnly = true;

    public RentalAdapter(Context context, List<Rental> rentals) {
        this.context = context;
        this.allRentals = rentals;
        this.filteredRentals = new ArrayList<>();
        filterRentals();
    }

    public void setRentals(List<Rental> rentals) {
        this.allRentals = rentals;
        filterRentals();
    }

    public void setShowCurrentRentals(boolean showCurrentRentalsOnly) {
        if (this.showCurrentRentalsOnly != showCurrentRentalsOnly) {
            this.showCurrentRentalsOnly = showCurrentRentalsOnly;
            filterRentals();
        }
    }

    private void filterRentals() {
        filteredRentals.clear();
        for (Rental rental : allRentals) {
            if ((showCurrentRentalsOnly && rental.isCurrent()) ||
                    (!showCurrentRentalsOnly && !rental.isCurrent())) {
                filteredRentals.add(rental);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RentalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rental_card, parent, false);
        return new RentalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RentalViewHolder holder, int position) {
        Rental rental = filteredRentals.get(position);

        holder.tvLocationName.setText(rental.getSpotName());
        holder.tvLocationAddress.setText(rental.getSpotLocation());

        // Set vehicle info
        String vehicleInfo = rental.getVehicleDescription() + " • " + rental.getLicensePlate();
        holder.tvVehicleInfo.setText(vehicleInfo);

        // Parse start and end times
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date startDate = sdf.parse(rental.getStartTime());
            Date endDate = sdf.parse(rental.getEndTime());

            // Format for display
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

            // Calculate duration
            if (startDate != null && endDate != null) {
                long durationMillis = endDate.getTime() - startDate.getTime();
                int hours = (int) (durationMillis / (1000 * 60 * 60));
                holder.tvDuration.setText(hours + " hour" + (hours != 1 ? "s" : ""));

                // Format end time
                holder.tvEndTime.setText(timeFormat.format(endDate) + " • " + dateFormat.format(endDate));
            } else {
                holder.tvDuration.setText("N/A");
                holder.tvEndTime.setText("N/A");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            holder.tvDuration.setText("N/A");
            holder.tvEndTime.setText("N/A");
        }

        // Format cost
        holder.tvTotalCost.setText("₱" + String.format(Locale.getDefault(), "%.2f", rental.getTotalCost()));

        // Set status based on time comparison
        boolean isActive = rental.isCurrent();
        holder.tvStatus.setText(isActive ? "Active" : "Expired");

        // Set status background color based on active/expired state
        int colorResId = isActive ?
                R.color.status_active_background :
                R.color.status_expired_background;
        holder.tvStatus.getBackground().setColorFilter(
                ContextCompat.getColor(context, colorResId),
                PorterDuff.Mode.SRC_IN);
    }

    @Override
    public int getItemCount() {
        return filteredRentals.size();
    }

    static class RentalViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvLocationName;
        TextView tvLocationAddress;
        TextView tvVehicleInfo;
        TextView tvDuration;
        TextView tvEndTime;
        TextView tvTotalCost;
        TextView tvStatus;

        RentalViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvLocationName = itemView.findViewById(R.id.tv_location_name);
            tvLocationAddress = itemView.findViewById(R.id.tv_location_address);
            tvVehicleInfo = itemView.findViewById(R.id.tv_vehicle_info);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvEndTime = itemView.findViewById(R.id.tv_end_time);
            tvTotalCost = itemView.findViewById(R.id.tv_total_cost);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}