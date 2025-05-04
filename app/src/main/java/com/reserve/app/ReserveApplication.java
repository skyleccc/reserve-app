package com.reserve.app;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class ReserveApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

    }
}