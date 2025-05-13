package com.reserve.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private TextView loginButton, loginGoogleButton, loginAppleButton, createAccountButton;
    private TextView header1TextView, header2TextView, footerTextView, errorMessageTextView;
    private TextView[] textTextViews = new TextView[2];
    private com.google.android.material.textfield.TextInputEditText emailEditText, passwordEditText;
    private com.google.android.material.textfield.TextInputLayout emailInputLayout, passwordInputLayout;
    private DatabaseHandler databaseHandler;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    SessionManager sessionManager;
    private static final int REQ_ONE_TAP = 2;
    private static final int REQUEST_LOCATION_PERMISSION = 1;


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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        // Initialize the database handler
        databaseHandler = DatabaseHandler.getInstance(this);

        // Check if user is already logged in
        user = databaseHandler.getCurrentUser();
        sessionManager = new SessionManager(LoginActivity.this);

        if (user != null && sessionManager.isLoggedIn()) {
            // Both Firebase Auth and SessionManager confirm user is logged in
            Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
            startActivity(intent);
            finish();
            return;
        } else if (user != null && !sessionManager.isLoggedIn()) {
            // Firebase says user is logged in but session data is missing
            // Need to populate the session data from Firestore
            databaseHandler.getUserData(user.getUid(), new DatabaseHandler.DocumentCallback() {
                @Override
                public void onSuccess(DocumentSnapshot document) {
                    if (document.exists()) {
                        // Existing code to save user session
                    } else {
                        // Handle missing user data
                        databaseHandler.signOut();
                        sessionManager.clearSession();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // Error retrieving data - sign out to be safe
                    databaseHandler.signOut();
                    sessionManager.clearSession();
                }
            });
            return;
        }

        initializeUI();

        loginButton.setOnClickListener(v -> { loginUserEmail(); });
        loginGoogleButton.setOnClickListener(v -> { loginUserGoogle(); });
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
                emailInputLayout.setError("Email is required");
                emailInputLayout.setErrorEnabled(true);
            } else {
                emailInputLayout.setErrorEnabled(false);
            }

            if (password.isEmpty()) {
                passwordInputLayout.setError("Password is required");
                passwordInputLayout.setErrorEnabled(true);
            } else {
                passwordInputLayout.setErrorEnabled(false);
            }

            return;
        }

        // Show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Logging In...");

        databaseHandler.signInUser(email, password, new DatabaseHandler.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Get additional user data from database
                databaseHandler.getUserData(user.getUid(), new DatabaseHandler.DocumentCallback() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if (document.exists()) {
                            // Save user session
                            sessionManager.saveUserSession(
                                    user.getUid(),
                                    document.getString("firstName"),
                                    document.getString("lastName"),
                                    document.getString("email"),
                                    document.getString("phone"),
                                    document.getString("authType")
                            );

                            // Navigate to homepage
                            Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle document retrieval failure
                        errorMessageTextView.setText("Failed to retrieve user data: " + e.getMessage());
                        errorMessageTextView.setVisibility(View.VISIBLE);
                        loginButton.setEnabled(true);
                        loginButton.setText("Continue");
                    }
                });
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

    private void loginUserGoogle() {
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile() // Request profile info to get name
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Show loading state
        loginGoogleButton.setEnabled(false);

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
                errorMessageTextView.setText("Google sign-in failed: " + e.getMessage());
                errorMessageTextView.setVisibility(View.VISIBLE);
                loginGoogleButton.setEnabled(true);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // Get the ID token from the Google Sign-In account
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        // Authenticate with Firebase using DatabaseHandler
        loginGoogleButton.setEnabled(false);
        databaseHandler.signInWithGoogle(credential, new DatabaseHandler.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Check if the user has phone number in Firestore
                checkUserProfileCompletion(user, account);
                loginGoogleButton.setEnabled(true);
            }

            @Override
            public void onFailure(Exception e) {
                loginGoogleButton.setEnabled(true);
                // Handle failure
                String errorMessage = e != null ? e.getMessage() : "Authentication failed";
                errorMessageTextView.setText("Authentication failed: " + errorMessage);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void checkUserProfileCompletion(FirebaseUser user, GoogleSignInAccount account) {
        databaseHandler.getUserData(user.getUid(), new DatabaseHandler.DocumentCallback() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                if (document.exists() && document.get("phone") != null) {
                    // User has complete profile, proceed to homepage
                    Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // User needs to complete their profile
                    String firstName = account.getGivenName() != null ? account.getGivenName() : "";
                    String lastName = account.getFamilyName() != null ? account.getFamilyName() : "";
                    String email = account.getEmail();
                    String idToken = account.getIdToken();

                    Intent intent = new Intent(LoginActivity.this, CompleteGoogleSignupActivity.class);
                    intent.putExtra("firstName", firstName);
                    intent.putExtra("lastName", lastName);
                    intent.putExtra("email", email);
                    intent.putExtra("idToken", idToken);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Couldn't verify user profile - proceed to completion screen to be safe
                String firstName = account.getGivenName() != null ? account.getGivenName() : "";
                String lastName = account.getFamilyName() != null ? account.getFamilyName() : "";
                String email = account.getEmail();
                String idToken = account.getIdToken();

                Intent intent = new Intent(LoginActivity.this, CompleteGoogleSignupActivity.class);
                intent.putExtra("firstName", firstName);
                intent.putExtra("lastName", lastName);
                intent.putExtra("email", email);
                intent.putExtra("idToken", idToken);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                // Consider showing a message about limited functionality
            }
        }
    }

    private void initializeUI() {
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
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

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