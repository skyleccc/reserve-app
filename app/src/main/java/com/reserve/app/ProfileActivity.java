package com.reserve.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
    TextView userName, userEmail, userPhone;
    LinearLayout accountBtn;
    TextView logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userEmail = findViewById(R.id.userEmail);
        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);

        accountBtn = findViewById(R.id.accountLayout);
        logoutBtn = findViewById(R.id.logoutBtn);

        initializeUI();

        accountBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditUser.class));
        });

        logoutBtn.setOnClickListener(v -> {
            signOut();
        });
    }

    private void signOut(){
        // Clear the session
        new SessionManager(this).clearSession();

        // Sign out from Firebase Auth
        FirebaseAuth.getInstance().signOut();

        // Also sign out from Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Navigate to login screen after all sign-out operations complete
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initializeUI() {
        // Session Manager
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            String firstName = sessionManager.getFirstName();
            String lastName = sessionManager.getLastName();
            String email = sessionManager.getEmail();
            String phone = sessionManager.getPhone();

            // Set user details in the UI
            userName.setText(firstName + " " + lastName);
            userEmail.setText(email);
            userPhone.setText("(+63) " + phone);

        } else {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}