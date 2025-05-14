package com.reserve.app;

public class ParkingSpot {
    String id;
    String title;
    String address;
    int imageResId;
    String price3Hours;
    String price6Hours;
    String price12Hours;
    String pricePerDay;

    public ParkingSpot(String id, String title, String address, int imageResId,
                       String price3Hours, String price6Hours, String price12Hours, String pricePerDay) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.imageResId = imageResId;
        this.price3Hours = price3Hours;
        this.price6Hours = price6Hours;
        this.price12Hours = price12Hours;
        this.pricePerDay = pricePerDay;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }



}