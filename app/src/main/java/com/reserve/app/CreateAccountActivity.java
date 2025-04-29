package com.reserve.app;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateAccountActivity extends AppCompatActivity {
    private TextView createAccountButton, createAccountGoogleButton, createAccountAppleButton, loginAccountButton;
    private TextView header1TextView, header2TextView, footerTextView;
    private TextView[] textTextViews = new TextView[2];
    private TextView firstNameEditText, lastNameEditText, emailEditText, passwordEditText, confirmPasswordEditText, phoneEditText;

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


        initializeUI();

        createAccountButton.setOnClickListener(v -> {;
            // Handle create account button click
        });

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