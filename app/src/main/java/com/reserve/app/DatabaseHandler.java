package com.reserve.app;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHandler {
    // Callback interfaces for async operations
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    public interface BooleanCallback {
        void onResult(boolean result);
        void onError(Exception e);
    }

    // Singleton instance
    private static DatabaseHandler instance;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Collection names
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_PARKING_SPACES = "parking_spaces";
    private static final String COLLECTION_RENTALS = "rentals";
    private static final String COLLECTION_MESSAGES = "messages";

    private DatabaseHandler(Context context) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized DatabaseHandler getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context.getApplicationContext());
        }
        return instance;
    }

    // User creation
    public void createUser(String firstName, String lastName, String email, String password, String phone, String authType, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        FirebaseUser user = task.getResult().getUser();
                        // Update display name on Auth and chain the task
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(firstName + " " + lastName)
                                .build();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    if (profileTask.isSuccessful()) {
                                        // Create corresponding Firestore document once profile update is successful
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("firstName", firstName);
                                        userData.put("lastName", lastName);
                                        userData.put("email", email);
                                        userData.put("phone", phone);
                                        userData.put("authType", authType);
                                        userData.put("createdAt", com.google.firebase.Timestamp.now());
                                        db.collection(COLLECTION_USERS).document(user.getUid())
                                                .set(userData)
                                                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                                                .addOnFailureListener(callback::onFailure);
                                    } else {
                                        callback.onFailure(profileTask.getException());
                                    }
                                });
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    // Email uniqueness check
    public void isEmailUnique(String email, BooleanCallback callback) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(task.getResult().isEmpty());
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    // Phone uniqueness check
    public void isPhoneUnique(String phone, BooleanCallback callback) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("phone", phone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(task.getResult().isEmpty());
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    // Parking space creation
    public void createParkingSpace(String name, String location, double rate3h,
                                   double rate6h, double rate12h, double rate24h,
                                   BooleanCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new IllegalStateException("User not logged in"));
            return;
        }

        Map<String, Object> parkingSpace = new HashMap<>();
        parkingSpace.put("name", name);
        parkingSpace.put("location", location);
        parkingSpace.put("rate3h", rate3h);
        parkingSpace.put("rate6h", rate6h);
        parkingSpace.put("rate12h", rate12h);
        parkingSpace.put("rate24h", rate24h);
        parkingSpace.put("userId", currentUser.getUid());
        parkingSpace.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection(COLLECTION_PARKING_SPACES)
                .add(parkingSpace)
                .addOnSuccessListener(doc -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onError(e));
    }

    // Rental creation
    public void createRental(String parkingSpaceId, String startTime,
                             String endTime, double totalCost, String status,
                             BooleanCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new IllegalStateException("User not logged in"));
            return;
        }

        Map<String, Object> rental = new HashMap<>();
        rental.put("userId", currentUser.getUid());
        rental.put("parkingSpaceId", parkingSpaceId);
        rental.put("startTime", startTime);
        rental.put("endTime", endTime);
        rental.put("totalCost", totalCost);
        rental.put("status", status);
        rental.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection(COLLECTION_RENTALS)
                .add(rental)
                .addOnSuccessListener(doc -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onError(e));
    }

    // Message creation
    public void createMessage(String receiverId, String content, BooleanCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new IllegalStateException("User not logged in"));
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", currentUser.getUid());
        message.put("receiverId", receiverId);
        message.put("content", content);
        message.put("timestamp", com.google.firebase.Timestamp.now());

        db.collection(COLLECTION_MESSAGES)
                .add(message)
                .addOnSuccessListener(doc -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onError(e));
    }

    // User profile update
    public void updateUserDetails(String firstName, String lastName, String phone,
                                  BooleanCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new IllegalStateException("User not logged in"));
            return;
        }

        // Update display name in Auth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(firstName + " " + lastName)
                .build();

        currentUser.updateProfile(profileUpdates);

        // Update Firestore user document
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("phone", phone);

        db.collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onError(e));
    }

    // Authentication methods
    public void signInUser(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}