package com.reserve.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class CreateAccountActivity extends AppCompatActivity {
    private TextView createAccountButton, createAccountGoogleButton, createAccountAppleButton, loginAccountButton;
    private TextView header1TextView, header2TextView, footerTextView, errorTextView;
    private TextView[] textTextViews = new TextView[2];
    private TextView firstNameEditText, lastNameEditText, emailEditText, passwordEditText, confirmPasswordEditText, phoneEditText;

    private DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        createAccountButton = findViewById(R.id.buttonCreateAccount);
        createAccountGoogleButton = findViewById(R.id.buttonCreateGoogle);
        createAccountAppleButton = findViewById(R.id.buttonCreateApple);
        header1TextView = findViewById(R.id.headerTextView);
        header2TextView = findViewById(R.id.header2TextView);
        textTextViews[0] = findViewById(R.id.textTextView1);
        textTextViews[1] = findViewById(R.id.textTextView2);
        firstNameEditText = findViewById(R.id.editTextFirstName);
        lastNameEditText = findViewById(R.id.editTextLastName);
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextTextConfirmPassword);
        phoneEditText = findViewById(R.id.editTextPhone);
        footerTextView = findViewById(R.id.footerTextView);
        loginAccountButton = findViewById(R.id.loginAccountTextView);
        errorTextView = findViewById(R.id.errorMessageTextView);

        // DB instance
        dbHandler = DatabaseHandler.getInstance(this);

        initializeUI();

        createAccountButton.setOnClickListener(v -> { resetPrevious(); createUser(); });

        createAccountGoogleButton.setOnClickListener(v -> {
            // Handle Google create account button click
        });

        createAccountAppleButton.setOnClickListener(v -> {
            // Handle Apple create account button click
        });

        loginAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void createUser(){
        // Handle create account button click
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        String phone = phoneEditText.getText().toString();

        if(firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
            // Lacking Credentials
            errorTextView.setText("Please fill in all fields.");
            errorTextView.setVisibility(View.VISIBLE);

            if(firstName.isEmpty()) { firstNameEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2"))); }
            if(lastName.isEmpty()) { lastNameEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2"))); }
            if(email.isEmpty()) { emailEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2"))); }
            if(password.isEmpty()) { passwordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2"))); }
            if(confirmPassword.isEmpty()) { confirmPasswordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2"))); }
            if(phone.isEmpty()) { phoneEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2"))); }

            return;
        }

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Invalid email format
            errorTextView.setText("Invalid email format.");
            errorTextView.setVisibility(View.VISIBLE);
            emailEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
            return;
        }

        if(!android.util.Patterns.PHONE.matcher(phone).matches())){
            // Invalid phone number format
            errorTextView.setText("Invalid phone number format.");
            errorTextView.setVisibility(View.VISIBLE);
            phoneEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
            return;
        }

        if(password.length() < 8){
            // Password too short
            errorTextView.setText("Password must be at least 8 characters.");
            errorTextView.setVisibility(View.VISIBLE);
            passwordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
            return;
        }

        if(!password.equals(confirmPassword)) {
            // Not the same password
            errorTextView.setText("Passwords do not match.");
            errorTextView.setVisibility(View.VISIBLE);
            passwordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
            confirmPasswordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
            return;
        }

        if(!dbHandler.isEmailUnique(email)) {
            // Email already exists
            errorTextView.setText("Email already exists.");
            errorTextView.setVisibility(View.VISIBLE);
            emailEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
            return;
        }

        if(!dbHandler.isPhoneUnique(phone)) {
            // Phone number already exists
            errorTextView.setText("Phone number already exists.");
            errorTextView.setVisibility(View.VISIBLE);
            phoneEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFCDD2")));
            return;
        }

        errorTextView.setVisibility(View.GONE);

        // Create account in the database
        long userID = dbHandler.createUser(firstName, lastName, email, password, phone, "Normal");
        if(userID != -1){
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else{
            Toast.makeText(CreateAccountActivity.this, "Error creating account", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPrevious(){
        // Reset the background tint of all EditText fields
        firstNameEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EBEBEB")));
        lastNameEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EBEBEB")));
        emailEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EBEBEB")));
        passwordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EBEBEB")));
        confirmPasswordEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EBEBEB")));
        phoneEditText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EBEBEB")));
    }

    private void initializeUI(){
        // fonts
        Typeface lexendExaSemiFont = Typeface.createFromAsset(getAssets(), "fonts/LexendExa/LexendExa-SemiBold.ttf");
        Typeface interSemiFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-SemiBold.ttf");
        Typeface interRegularFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-Regular.ttf");
        Typeface interMediumFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-Medium.ttf");

        createAccountButton.setTypeface(interSemiFont);
        createAccountGoogleButton.setTypeface(interSemiFont);
        createAccountAppleButton.setTypeface(interSemiFont);
        header1TextView.setTypeface(lexendExaSemiFont);
        header2TextView.setTypeface(interSemiFont);
        textTextViews[0].setTypeface(interRegularFont);
        textTextViews[1].setTypeface(interRegularFont);
        firstNameEditText.setTypeface(interRegularFont);
        lastNameEditText.setTypeface(interRegularFont);
        emailEditText.setTypeface(interRegularFont);
        passwordEditText.setTypeface(interRegularFont);
        confirmPasswordEditText.setTypeface(interRegularFont);
        phoneEditText.setTypeface(interRegularFont);
        footerTextView.setTypeface(interRegularFont);
        loginAccountButton.setTypeface(interMediumFont);
        errorTextView.setTypeface(interMediumFont);

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

        createAccountGoogleButton.setText(loginGoogleText);
        createAccountAppleButton.setText(loginAppleText);
    }
}