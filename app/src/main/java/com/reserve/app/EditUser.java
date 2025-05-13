package com.reserve.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.Map;

public class EditUser extends AppCompatActivity {
    EditText et_fname, et_lname, et_email, et_phone, et_password, et_confirm_password;
    TextView confirmChange;
    Map<String, Object> updates = new HashMap<>();
    private DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user);

        et_fname = findViewById(R.id.editFirstName);
        et_lname = findViewById(R.id.editLastName);
        et_email = findViewById(R.id.editEmail);
        et_phone = findViewById(R.id.editNumber);
        et_password = findViewById(R.id.editPassword);
        et_confirm_password = findViewById(R.id.editConfirmPassword);
        confirmChange = findViewById(R.id.confirmChangeBtn);

        dbHandler = DatabaseHandler.getInstance(this);

        confirmChange.setOnClickListener(v -> {
            String firstName = et_fname.getText().toString();
            String lastName = et_lname.getText().toString();
            String email = et_email.getText().toString();
            String phone = et_phone.getText().toString();
            String password = et_password.getText().toString();
            String confirmPassword = et_confirm_password.getText().toString();

            if (!phone.isEmpty() && !android.util.Patterns.PHONE.matcher(phone).matches()) {
                Toast.makeText(this, "Invalid phone number format.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!firstName.isEmpty()) {
                updates.put("firstName", firstName);
            }
            if (!lastName.isEmpty()) {
                updates.put("lastName", lastName);
            }
            if (!email.isEmpty()) {
                updates.put("email", email);
            }
            if (!phone.isEmpty()) {
                updates.put("phone", phone);
            }
            if (!password.isEmpty()) {
                if (password.equals(confirmPassword)) {
                    updates.put("password", password);
                } else {
                    Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            dbHandler.updateUserDetails(updates, new DatabaseHandler.BooleanCallback() {
                @Override
                public void onResult(boolean success) {
                    if (success) {
                        Toast.makeText(EditUser.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditUser.this, "Nothing to update", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(EditUser.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}