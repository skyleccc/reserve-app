package com.reserve.app;

public class ParkingSpot {
    String title;
    String address;
    int imageResId;
    String priceFirstHour;
    String priceExceeding;
    String pricePerDay;

    public ParkingSpot(String title, String address, int imageResId,
                       String priceFirstHour, String priceExceeding, String pricePerDay) {
        this.title = title;
        this.address = address;
        this.imageResId = imageResId;
        this.priceFirstHour = priceFirstHour;
        this.priceExceeding = priceExceeding;
        this.pricePerDay = pricePerDay;
    }
}