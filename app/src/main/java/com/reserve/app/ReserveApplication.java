package com.reserve.app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;

public class ReserveApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Firebase initialization
        FirebaseApp.initializeApp(this);

        // Set default night mode to light
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

    }
}