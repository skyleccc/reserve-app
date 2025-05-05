package com.reserve.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextView loginButton, loginGoogleButton, loginAppleButton, createAccountButton;
    private TextView header1TextView, header2TextView, footerTextView, errorMessageTextView;
    private TextView[] textTextViews = new TextView[2];
    private EditText emailEditText, passwordEditText;
    private DatabaseHandler databaseHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if user is already logged in
        boolean loggedIn = getSharedPreferences("app", MODE_PRIVATE).getBoolean("logged_in", false);
        if (loggedIn) {
            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize UI components
        loginButton = findViewById(R.id.buttonSignIn);
        loginGoogleButton = findViewById(R.id.buttonGoogle);
        loginAppleButton = findViewById(R.id.buttonApple);
        header1TextView = findViewById(R.id.headerTextView);
        header2TextView = findViewById(R.id.header2TextView);
        textTextViews[0] = findViewById(R.id.textTextView1);
        textTextViews[1] = findViewById(R.id.textTextView2);
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        createAccountButton = findViewById(R.id.createAccountTextView);
        footerTextView = findViewById(R.id.footerTextView);
        errorMessageTextView = findViewById(R.id.errorMessageTextView);

        initializeUI();


        loginButton.setOnClickListener(v -> { loginUserEmail(); });

        loginGoogleButton.setOnClickListener(v -> {
            // Handle Google login button click
        });

        loginAppleButton.setOnClickListener(v -> {
            // Handle Apple login button click
        });

        createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });
    }

    private void loginUserEmail() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            // Lacking Credentials
            errorMessageTextView.setText("Please fill in all fields.");
            errorMessageTextView.setVisibility(View.VISIBLE);

            if (email.isEmpty()) {
                emailEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
                emailEditText.setError("Email is required");
            };

            if (password.isEmpty()) {
                passwordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
                passwordEditText.setError("Password is required");
            };

            return;
        }

        // Show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Logging In...");

        databaseHandler = DatabaseHandler.getInstance(this);
        databaseHandler.signInUser(email, password, new DatabaseHandler.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Handle successful login
                getSharedPreferences("app", MODE_PRIVATE).edit().putBoolean("logged_in", true).apply();
                Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                // Handle login failure
                errorMessageTextView.setText("Invalid credentials. Please try again.");
                errorMessageTextView.setVisibility(View.VISIBLE);
                loginButton.setEnabled(true);
                loginButton.setText("Continue");
            }
        });
    }

    private void initializeUI() {
        // fonts
        Typeface lexendExaSemiFont = Typeface.createFromAsset(getAssets(), "fonts/LexendExa/LexendExa-SemiBold.ttf");
        Typeface interSemiFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-SemiBold.ttf");
        Typeface interRegularFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-Regular.ttf");
        Typeface interMediumFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-Medium.ttf");

        header1TextView.setTypeface(lexendExaSemiFont);
        header2TextView.setTypeface(interSemiFont);
        loginButton.setTypeface(interMediumFont);
        loginGoogleButton.setTypeface(interMediumFont);
        loginAppleButton.setTypeface(interMediumFont);
        textTextViews[0].setTypeface(interRegularFont);
        textTextViews[1].setTypeface(interRegularFont);
        emailEditText.setTypeface(interRegularFont);
        passwordEditText.setTypeface(interRegularFont);
        createAccountButton.setTypeface(interMediumFont);
        footerTextView.setTypeface(interRegularFont);
        errorMessageTextView.setTypeface(interMediumFont);

        // icons beside the buttons
        float density = getResources().getDisplayMetrics().density;
        int iconSizeInDp = 14;
        int iconSize = (int) (iconSizeInDp * density);

        String googleText = "\uFFFC Continue with Google";
        String appleText = "\uFFFC Continue with Apple";

        SpannableString loginGoogleText = new SpannableString(googleText);
        SpannableString loginAppleText = new SpannableString(appleText);

        Drawable googleIcon = getResources().getDrawable(R.drawable.ic_google);
        googleIcon.setBounds(0, 0, iconSize, iconSize);
        Drawable appleIcon = getResources().getDrawable(R.drawable.ic_apple);
        appleIcon.setBounds(0, 0, iconSize, iconSize);

        loginGoogleText.setSpan(new ImageSpan(googleIcon, ImageSpan.ALIGN_CENTER),
                0, 1,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        loginAppleText.setSpan(new ImageSpan(appleIcon, ImageSpan.ALIGN_CENTER),
                0, 1,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        loginGoogleButton.setText(loginGoogleText);
        loginAppleButton.setText(loginAppleText);
    }
}