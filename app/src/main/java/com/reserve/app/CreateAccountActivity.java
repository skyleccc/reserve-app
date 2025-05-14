package com.reserve.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class CreateAccountActivity extends AppCompatActivity {
    private TextView createAccountButton, createAccountGoogleButton, loginAccountButton;
    private TextView header1TextView, header2TextView, footerTextView, errorTextView;
    private TextInputEditText firstNameEditText, lastNameEditText, emailEditText,
            passwordEditText, confirmPasswordEditText, phoneEditText;
    private TextInputLayout firstNameLayout, lastNameLayout, emailLayout,
            passwordLayout, confirmPasswordLayout, phoneLayout;
    private TextView[] textTextViews = new TextView[2];

    private DatabaseHandler dbHandler;
    private static final int REQ_ONE_TAP = 2;

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
        header1TextView = findViewById(R.id.headerTextView);
        header2TextView = findViewById(R.id.header2TextView);
        textTextViews[0] = findViewById(R.id.textTextView1);
        textTextViews[1] = findViewById(R.id.textTextView2);

        // Get TextInputEditText references
        firstNameEditText = findViewById(R.id.editTextFirstName);
        lastNameEditText = findViewById(R.id.editTextLastName);
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextTextConfirmPassword);
        phoneEditText = findViewById(R.id.editTextPhone);

        // Get TextInputLayout references
        firstNameLayout = findViewById(R.id.firstNameInputLayout);
        lastNameLayout = findViewById(R.id.lastNameInputLayout);
        emailLayout = findViewById(R.id.emailInputLayout);
        passwordLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordInputLayout);
        phoneLayout = findViewById(R.id.phoneInputLayout);

        footerTextView = findViewById(R.id.footerTextView);
        loginAccountButton = findViewById(R.id.loginAccountTextView);
        errorTextView = findViewById(R.id.errorMessageTextView);

        // DB instance
        dbHandler = DatabaseHandler.getInstance(this);

        initializeUI();

        createAccountButton.setOnClickListener(v -> {
            resetPrevious();
            createUser();
        });

        createAccountGoogleButton.setOnClickListener(v -> {
            resetPrevious();
            createUserGoogle();
        });

        loginAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void createUser() {
        // Handle create account button click
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        String phone = phoneEditText.getText().toString();

        // Reset all errors
        firstNameLayout.setErrorEnabled(false);
        lastNameLayout.setErrorEnabled(false);
        emailLayout.setErrorEnabled(false);
        passwordLayout.setErrorEnabled(false);
        confirmPasswordLayout.setErrorEnabled(false);
        phoneLayout.setErrorEnabled(false);

        if(firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
            // Lacking Credentials
            errorTextView.setText("Please fill in all fields.");
            errorTextView.setVisibility(View.VISIBLE);

            if(firstName.isEmpty()) { firstNameLayout.setError("First name is required"); }
            if(lastName.isEmpty()) { lastNameLayout.setError("Last name is required"); }
            if(email.isEmpty()) { emailLayout.setError("Email is required"); }
            if(password.isEmpty()) { passwordLayout.setError("Password is required"); }
            if(confirmPassword.isEmpty()) { confirmPasswordLayout.setError("Password confirmation is required"); }
            if(phone.isEmpty()) { phoneLayout.setError("Phone number is required"); }

            return;
        }

        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Invalid email format
            errorTextView.setText("Invalid email format.");
            errorTextView.setVisibility(View.VISIBLE);
            emailLayout.setError("Invalid email format");
            return;
        }

        if(!android.util.Patterns.PHONE.matcher(phone).matches()){
            // Invalid phone number format
            errorTextView.setText("Invalid phone number format.");
            errorTextView.setVisibility(View.VISIBLE);
            phoneLayout.setError("Invalid phone number format");
            return;
        }

        if(password.length() < 8){
            // Password too short
            errorTextView.setText("Password must be at least 8 characters.");
            errorTextView.setVisibility(View.VISIBLE);
            passwordLayout.setError("Password must be at least 8 characters");
            return;
        }

        if(!password.equals(confirmPassword)) {
            // Not the same password
            errorTextView.setText("Passwords do not match.");
            errorTextView.setVisibility(View.VISIBLE);
            passwordLayout.setError("Passwords do not match");
            confirmPasswordLayout.setError("Passwords do not match");
            return;
        }

        // Show loading state
        createAccountButton.setEnabled(false);
        createAccountButton.setText("Creating Account...");

        // First check if email is unique
        dbHandler.isEmailUnique(email, new DatabaseHandler.BooleanCallback() {
            @Override
            public void onResult(boolean isUnique) {
                if (!isUnique) {
                    runOnUiThread(() -> {
                        errorTextView.setText("Email already exists.");
                        errorTextView.setVisibility(View.VISIBLE);
                        emailLayout.setError("Email already exists");
                        createAccountButton.setEnabled(true);
                        createAccountButton.setText("Continue");
                    });
                    return;
                }

                // Then check if phone is unique
                dbHandler.isPhoneUnique(phone, new DatabaseHandler.BooleanCallback() {
                    @Override
                    public void onResult(boolean isUnique) {
                        if (!isUnique) {
                            runOnUiThread(() -> {
                                errorTextView.setText("Phone number already exists.");
                                errorTextView.setVisibility(View.VISIBLE);
                                phoneLayout.setError("Phone number already exists");
                                createAccountButton.setEnabled(true);
                                createAccountButton.setText("Continue");
                            });
                            return;
                        }

                        // If both checks pass, create the user
                        dbHandler.createUser(firstName, lastName, email, password, phone, "Normal",
                                new DatabaseHandler.AuthCallback() {
                                    @Override
                                    public void onSuccess(FirebaseUser user) {
                                        // Create session
                                        SessionManager sessionManager = new SessionManager(CreateAccountActivity.this);
                                        sessionManager.saveUserSession(
                                                user.getUid(),
                                                firstName,
                                                lastName,
                                                email,
                                                phone,
                                                "Normal"
                                        );

                                        runOnUiThread(() -> {
                                            Toast.makeText(CreateAccountActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
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
                                            createAccountButton.setEnabled(true);
                                            createAccountButton.setText("Continue");
                                        });
                                    }
                                });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            errorTextView.setText("Error checking phone: " + e.getMessage());
                            errorTextView.setVisibility(View.VISIBLE);
                            createAccountButton.setEnabled(true);
                            createAccountButton.setText("Continue");
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    errorTextView.setText("Error checking email: " + e.getMessage());
                    errorTextView.setVisibility(View.VISIBLE);
                    createAccountButton.setEnabled(true);
                    createAccountButton.setText("Continue");
                });
            }
        });
    }

    private void createUserGoogle() {
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile() // Request profile info to get name
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Start the Google Sign-In intent
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQ_ONE_TAP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                errorTextView.setText("Google sign-in failed: " + e.getMessage());
                errorTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // Get the ID token from the Google Sign-In account
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        String email = account.getEmail();

        // Show loading state
        createAccountGoogleButton.setEnabled(false);
        createAccountGoogleButton.setText("Checking account...");

        // First check if email already exists in the database
        dbHandler.isEmailUnique(email, new DatabaseHandler.BooleanCallback() {
            @Override
            public void onResult(boolean isUnique) {
                if (!isUnique) {
                    // Email already exists
                    runOnUiThread(() -> {
                        initializeUI();
                        errorTextView.setText("An account with this email already exists.\nPlease use the login option.");
                        createAccountGoogleButton.setEnabled(true);
                        errorTextView.setVisibility(View.VISIBLE);
                    });
                    // Sign out from the current Google sign-in attempt
                    dbHandler.signOut(); // Use DatabaseHandler instead of direct Firebase call
                    return;
                }

                // Continue with Google sign-in process
                // Get user profile data from Google account
                String firstName = account.getGivenName() != null ? account.getGivenName() : "";
                String lastName = account.getFamilyName() != null ? account.getFamilyName() : "";
                String email = account.getEmail();
                String idToken = account.getIdToken();

                // Start CompleteGoogleSignupActivity for user to provide phone
                Intent intent = new Intent(CreateAccountActivity.this, CompleteGoogleSignupActivity.class);
                intent.putExtra("firstName", firstName);
                intent.putExtra("lastName", lastName);
                intent.putExtra("email", email);
                intent.putExtra("idToken", idToken);
                startActivity(intent);
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    errorTextView.setText("Error checking email: " + e.getMessage());
                    errorTextView.setVisibility(View.VISIBLE);
                    createAccountGoogleButton.setEnabled(true);
                    createAccountGoogleButton.setText("Sign up with Google");
                });
            }
        });
    }

    private void resetPrevious() {
        // Reset all input layouts
        if (firstNameLayout != null) firstNameLayout.setErrorEnabled(false);
        if (lastNameLayout != null) lastNameLayout.setErrorEnabled(false);
        if (emailLayout != null) emailLayout.setErrorEnabled(false);
        if (passwordLayout != null) passwordLayout.setErrorEnabled(false);
        if (confirmPasswordLayout != null) confirmPasswordLayout.setErrorEnabled(false);
        if (phoneLayout != null) phoneLayout.setErrorEnabled(false);
        errorTextView.setVisibility(View.GONE);
    }

    private void initializeUI() {
        // fonts
        Typeface lexendExaSemiFont = Typeface.createFromAsset(getAssets(), "fonts/LexendExa/LexendExa-SemiBold.ttf");
        Typeface interSemiFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-SemiBold.ttf");
        Typeface interRegularFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-Regular.ttf");
        Typeface interMediumFont = Typeface.createFromAsset(getAssets(), "fonts/Inter/Inter_18pt-Medium.ttf");

        createAccountButton.setTypeface(interSemiFont);
        createAccountGoogleButton.setTypeface(interSemiFont);
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

        SpannableString loginGoogleText = new SpannableString(googleText);

        Drawable googleIcon = getResources().getDrawable(R.drawable.ic_google);
        googleIcon.setBounds(0, 0, iconSize, iconSize);

        loginGoogleText.setSpan(new ImageSpan(googleIcon, ImageSpan.ALIGN_CENTER),
                0, 1,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        createAccountGoogleButton.setText(loginGoogleText);
    }
}