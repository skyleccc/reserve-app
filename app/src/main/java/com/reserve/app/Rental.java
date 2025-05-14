package com.reserve.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Rental {
    private String id;
    private String parkingSpaceId;
    private String spotName;
    private String spotLocation;
    private String startTime;
    private String endTime;
    private double totalCost;
    private String status;
    private String licensePlate;
    private String vehicleDescription;

    public Rental(String id, String parkingSpaceId, String spotName, String spotLocation,
                  String startTime, String endTime, double totalCost,
                  String status, String licensePlate, String vehicleDescription) {
        this.id = id;
        this.parkingSpaceId = parkingSpaceId;
        this.spotName = spotName;
        this.spotLocation = spotLocation;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalCost = totalCost;
        this.status = status;
        this.licensePlate = licensePlate;
        this.vehicleDescription = vehicleDescription;
    }

    // Getters
    public String getId() { return id; }
    public String getParkingSpaceId() { return parkingSpaceId; }
    public String getSpotName() { return spotName; }
    public String getSpotLocation() { return spotLocation; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public double getTotalCost() { return totalCost; }
    public String getStatus() { return status; }
    public String getLicensePlate() { return licensePlate; }
    public String getVehicleDescription() { return vehicleDescription; }

    public boolean isCurrent() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date endDate = sdf.parse(endTime);
            Date currentTime = new Date(); // Current time

            // Rental is current if end time is in the future
            return endDate != null && endDate.after(currentTime);
        } catch (ParseException e) {
            e.printStackTrace();
            // If parsing fails, fall back to status check
            return "Pending".equals(status) || "Active".equals(status);
        }
    }
}