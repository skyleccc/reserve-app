package com.reserve.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        final boolean[] keepSplashScreenVisible = {true};
        splashScreen.setKeepOnScreenCondition(() -> keepSplashScreenVisible[0]);
        new Handler().postDelayed(() -> {
            keepSplashScreenVisible[0] = false;

            // Check if user is already logged in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            SessionManager sessionManager = new SessionManager(MainActivity.this);

            Intent intent;
            if (user != null && sessionManager.isLoggedIn()) {
                intent = new Intent(MainActivity.this, HomepageActivity.class);
            } else {
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, 1000);
    }
}