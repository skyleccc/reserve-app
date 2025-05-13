package com.reserve.app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;

public class ReserveApplication extends Application {
    private LocationTracker locationTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        // Firebase initialization
        FirebaseApp.initializeApp(this);

        // Set default night mode to light
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Location Tracker
        locationTracker = LocationTracker.getInstance(this);

    }

    public void startLocationTracking(LocationTracker.LocationUpdateListener listener) {
        if (locationTracker != null) {
            locationTracker.startLocationUpdates(listener);
        }
    }

    public LocationTracker getLocationTracker() {
        return locationTracker;
    }
}