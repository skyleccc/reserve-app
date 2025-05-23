package com.reserve.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class CompleteGoogleSignupActivity extends AppCompatActivity {
    private TextView completeAccountButton;
    private TextView header1TextView, header2TextView, errorTextView;
    private TextInputEditText firstNameEditText, lastNameEditText, emailEditText, phoneEditText;
    private TextInputLayout firstNameLayout, lastNameLayout, emailLayout, phoneLayout;

    private DatabaseHandler dbHandler;
    private String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complete_google_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        completeAccountButton = findViewById(R.id.buttonCompleteAccount);
        header1TextView = findViewById(R.id.headerTextView);
        header2TextView = findViewById(R.id.header2TextView);

        // Get TextInputEditText references
        firstNameEditText = findViewById(R.id.editTextFirstName);
        lastNameEditText = findViewById(R.id.editTextLastName);
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        phoneEditText = findViewById(R.id.editTextPhone);

        // Get TextInputLayout references
        firstNameLayout = findViewById(R.id.firstNameInputLayout);
        lastNameLayout = findViewById(R.id.lastNameInputLayout);
        emailLayout = findViewById(R.id.emailInputLayout);
        phoneLayout = findViewById(R.id.phoneInputLayout);

        errorTextView = findViewById(R.id.errorMessageTextView);

        // DB instance
        dbHandler = DatabaseHandler.getInstance(this);

        // Get data from intent
        Intent intent = getIntent();
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String email = intent.getStringExtra("email");
        idToken = intent.getStringExtra("idToken");

        // Pre-fill fields with Google data
        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);
        emailEditText.setText(email);
        emailEditText.setEnabled(false); // Make email field read-only

        initializeUI();

        completeAccountButton.setOnClickListener(v -> {
            resetPrevious();
            completeSignup();
        });
    }

    private void completeSignup() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String phone = phoneEditText.getText().toString();

        // Reset all errors
        firstNameLayout.setErrorEnabled(false);
        lastNameLayout.setErrorEnabled(false);
        emailLayout.setErrorEnabled(false);
        phoneLayout.setErrorEnabled(false);

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            // Lacking Credentials
            errorTextView.setText("Please fill in all fields.");
            errorTextView.setVisibility(View.VISIBLE);

            if (firstName.isEmpty()) { firstNameLayout.setError("First name is required"); }
            if (lastName.isEmpty()) { lastNameLayout.setError("Last name is required"); }
            if (phone.isEmpty()) { phoneLayout.setError("Phone number is required"); }

            return;
        }

        if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            // Invalid phone number format
            errorTextView.setText("Invalid phone number format.");
            errorTextView.setVisibility(View.VISIBLE);
            phoneLayout.setError("Invalid phone number format");
            return;
        }

        // Show loading state
        completeAccountButton.setEnabled(false);
        completeAccountButton.setText("Creating Account...");

        // Check if phone is unique
        dbHandler.isPhoneUnique(phone, new DatabaseHandler.BooleanCallback() {
            @Override
            public void onResult(boolean isUnique) {
                if (!isUnique) {
                    runOnUiThread(() -> {
                        errorTextView.setText("Phone number already exists.");
                        errorTextView.setVisibility(View.VISIBLE);
                        phoneLayout.setError("Phone number already exists");
                        completeAccountButton.setEnabled(true);
                        completeAccountButton.setText("Complete Signup");
                    });
                    return;
                }

                // Create auth credential from the token
                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

                // Create user with Google
                dbHandler.createUserWithGoogle(credential, firstName, lastName, email, phone, new DatabaseHandler.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        // Create session
                        SessionManager sessionManager = new SessionManager(CompleteGoogleSignupActivity.this);
                        sessionManager.saveUserSession(
                                user.getUid(),
                                firstName,
                                lastName,
                                email,
                                phone,
                                "Google"
                        );

                        runOnUiThread(() -> {
                            Toast.makeText(CompleteGoogleSignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CompleteGoogleSignupActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> {
                            errorTextView.setText("Failed to create account: " + e.getMessage());
                            errorTextView.setVisibility(View.VISIBLE);
                            completeAccountButton.setEnabled(true);
                            completeAccountButton.setText("Complete Signup");
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    errorTextView.setText("Error checking phone: " + e.getMessage());
                    errorTextView.setVisibility(View.VISIBLE);
                    completeAccountButton.setEnabled(true);
                    completeAccountButton.setText("Complete Signup");
                });
            }
        });
    }

    private void resetPrevious() {
        if (firstNameLayout != null) firstNameLayout.setErrorEnabled(false);
        if (lastNameLayout != null) lastNameLayout.setErrorEnabled(false);
        if (emailLayout != null) emailLayout.setErrorEnabled(false);
        if (phoneLayout != null) phoneLayout.setErrorEnabled(false);
        errorTextView.setVisibility(View.GONE);
    }

    private void initializeUI() {
        // Set fonts
        Typeface lexendExaSemiFont = Typeface.createFromAsset(getAssets(), "fonts/LexendExa/LexendExa-SemiBold.ttf");
        Typeface interSemiFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-SemiBold.ttf");
        Typeface interRegularFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-Regular.ttf");
        Typeface interMediumFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-Medium.ttf");

        completeAccountButton.setTypeface(interSemiFont);
        header1TextView.setTypeface(lexendExaSemiFont);
        header2TextView.setTypeface(interSemiFont);
        firstNameEditText.setTypeface(interRegularFont);
        lastNameEditText.setTypeface(interRegularFont);
        emailEditText.setTypeface(interRegularFont);
        phoneEditText.setTypeface(interRegularFont);
        errorTextView.setTypeface(interMediumFont);
    }
}