package com.reserve.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class EditUser extends AppCompatActivity {
    private TextInputEditText et_fname, et_lname, et_email, et_phone, et_password, et_confirm_password;
    private TextInputLayout til_email;
    private MaterialButton saveButton, backButton;
    private ProgressBar progressBar;
    private Map<String, Object> updates = new HashMap<>();
    private DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        et_fname = findViewById(R.id.editFirstName);
        et_lname = findViewById(R.id.editLastName);
        et_email = findViewById(R.id.editEmail);
        til_email = findViewById(R.id.til_email);
        et_phone = findViewById(R.id.editNumber);
        et_password = findViewById(R.id.editPassword);
        et_confirm_password = findViewById(R.id.editConfirmPassword);
        saveButton = findViewById(R.id.btn_save_changes);
        backButton = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        // Disable email field
        til_email.setEnabled(false);
        et_email.setEnabled(false);

        dbHandler = DatabaseHandler.getInstance(this);

        // Back button click listener
        backButton.setOnClickListener(v -> finish());

        // Load current user data
        loadUserData();

        // Save button click listener
        saveButton.setOnClickListener(v -> saveUserChanges());
    }

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);

        // Get current user data from database
        String currentUserId = dbHandler.getCurrentUser().getUid();
        dbHandler.getUserData(currentUserId, new DatabaseHandler.DocumentCallback() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                if (document.exists()) {
                    // Populate fields with existing data
                    et_fname.setText(document.getString("firstName"));
                    et_lname.setText(document.getString("lastName"));
                    et_email.setText(document.getString("email"));
                    et_phone.setText(document.getString("phone"));

                    // Password fields remain empty for security
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EditUser.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void saveUserChanges() {
        String firstName = et_fname.getText().toString().trim();
        String lastName = et_lname.getText().toString().trim();
        String phone = et_phone.getText().toString().trim();
        String password = et_password.getText().toString();
        String confirmPassword = et_confirm_password.getText().toString();

        // Validate inputs
        if (!phone.isEmpty() && !android.util.Patterns.PHONE.matcher(phone).matches()) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create updates map
        updates.clear();
        if (!firstName.isEmpty()) {
            updates.put("firstName", firstName);
        }
        if (!lastName.isEmpty()) {
            updates.put("lastName", lastName);
        }
        if (!phone.isEmpty()) {
            updates.put("phone", phone);
        }
        if (!password.isEmpty()) {
            if (password.equals(confirmPassword)) {
                if (password.length() < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                updates.put("password", password);
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (updates.isEmpty()) {
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading and disable save button
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // Update user details
        dbHandler.updateUserDetails(updates, new DatabaseHandler.BooleanCallback() {
            @Override
            public void onResult(boolean success) {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);

                if (success) {
                    Toast.makeText(EditUser.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                    // Clear password fields for security
                    et_password.setText("");
                    et_confirm_password.setText("");

                    // Reload user data to show updated values
                    loadUserData();
                } else {
                    Toast.makeText(EditUser.this, "Nothing to update", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                Toast.makeText(EditUser.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}