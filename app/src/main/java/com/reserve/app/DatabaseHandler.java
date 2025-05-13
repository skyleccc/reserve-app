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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public interface DocumentCallback {
        void onSuccess(DocumentSnapshot document);
        void onFailure(Exception e);
    }

    public interface AllParkingSpotsCallback {
        void onSuccess(List<ParkingSpot> spots);
        void onFailure(Exception e);
    }

    public interface ParkingSpotsCallback {
        void onSuccess(List<ParkingSpot> spots, String[] spotIds);
        void onFailure(Exception e);
    }

    public interface ParkingSpotCallback {
        void onSuccess(DocumentSnapshot document);
        void onFailure(Exception e);
    }

    public interface ParkingSpotWithIDCallback {
        void onSuccess(List<DocumentSnapshot> documents);
        void onFailure(Exception e);
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

    public void getUserData(String userId, DocumentCallback callback) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public void updateUserLocation(String userId, double latitude, double longitude, BooleanCallback callback) {
        if (userId == null) {
            callback.onError(new IllegalArgumentException("User ID cannot be null"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("latitude", latitude);
        updates.put("longitude", longitude);

        db.collection(COLLECTION_USERS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onResult(true))
                .addOnFailureListener(callback::onError);
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

    public void createUserWithGoogle(AuthCredential credential, String firstName, String lastName, String email, String phone, AuthCallback callback) {
        mAuth.signInWithCredential(credential)
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
                                        userData.put("authType", "Google");
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


    public void getAllParkingSpots(boolean forceRefresh, AllParkingSpotsCallback callback) {
        // Source parameter determines if data comes from cache first
        Source source = forceRefresh ? Source.SERVER : Source.DEFAULT;

        db.collection(COLLECTION_PARKING_SPACES)
                .get(source)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ParkingSpot> spots = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString("name");
                        String location = doc.getString("location");
                        Double rate3h = doc.getDouble("rate3h");
                        Double rate6h = doc.getDouble("rate6h");
                        Double rate12h = doc.getDouble("rate12h");
                        Double rate24h = doc.getDouble("rate24h");

                        String price3Hours = "₱" + String.format("%.2f", rate3h);
                        String price6Hours = "₱" + String.format("%.2f", rate6h);
                        String price12Hours = "₱" + String.format("%.2f", rate12h);
                        String pricePerDay = "₱" + String.format("%.2f", rate24h);

                        spots.add(new ParkingSpot(name, location,
                                R.drawable.ic_map_placeholder, price3Hours,
                                price6Hours, price12Hours, pricePerDay));
                    }

                    callback.onSuccess(spots);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllParkingSpotsWithIDs(boolean forceRefresh, ParkingSpotWithIDCallback callback) {
        Source source = forceRefresh ? Source.SERVER : Source.DEFAULT;

        db.collection(COLLECTION_PARKING_SPACES)
                .get(source)
                .addOnSuccessListener(queryDocumentSnapshots ->
                        callback.onSuccess(queryDocumentSnapshots.getDocuments()))
                .addOnFailureListener(callback::onFailure);
    }

    public void getUserParkingSpots(boolean forceRefresh, ParkingSpotsCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure(new IllegalStateException("User not logged in"));
            return;
        }

        // Source parameter determines if data comes from cache first
        Source source = forceRefresh ? Source.SERVER : Source.DEFAULT;

        db.collection(COLLECTION_PARKING_SPACES)
                .whereEqualTo("userId", currentUser.getUid())
                .get(source)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ParkingSpot> spots = new ArrayList<>();
                    String[] spotIds = new String[queryDocumentSnapshots.size()];

                    int i = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString("name");
                        String location = doc.getString("location");
                        Double rate3h = doc.getDouble("rate3h");
                        Double rate6h = doc.getDouble("rate6h");
                        Double rate12h = doc.getDouble("rate12h");
                        Double rate24h = doc.getDouble("rate24h");

                        String price3Hours = "₱" + String.format("%.2f", rate3h);
                        String price6Hours = "₱" + String.format("%.2f", rate6h);
                        String price12Hours = "₱" + String.format("%.2f", rate12h);
                        String pricePerDay = "₱" + String.format("%.2f", rate24h);

                        spots.add(new ParkingSpot(name, location,
                                R.drawable.ic_map_placeholder, price3Hours,
                                price6Hours, price12Hours, pricePerDay));

                        spotIds[i++] = doc.getId();
                    }

                    callback.onSuccess(spots, spotIds);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getParkingSpotById(String spotId, ParkingSpotCallback callback) {
        db.collection(COLLECTION_PARKING_SPACES)
                .document(spotId)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public void updateParkingSpot(String spotId, String name, String location,
                                  double rate3h, double rate6h,
                                  double rate12h, double rate24h, BooleanCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("location", location);
        updates.put("rate3h", rate3h);
        updates.put("rate6h", rate6h);
        updates.put("rate12h", rate12h);
        updates.put("rate24h", rate24h);

        db.collection(COLLECTION_PARKING_SPACES)
                .document(spotId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onResult(true))
                .addOnFailureListener(callback::onError);
    }

    public void deleteParkingSpot(String spotId, BooleanCallback callback) {
        db.collection(COLLECTION_PARKING_SPACES)
                .document(spotId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onResult(true))
                .addOnFailureListener(callback::onError);
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
    public void updateUserDetails(Map<String, Object> updates,
                                  BooleanCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new IllegalStateException("User not logged in"));
            return;
        }

        // Update the following firebase auth fields only if present
        if (updates.containsKey("firstName") || updates.containsKey("lastName")) {
            String firstName = (String) updates.getOrDefault("firstName", "");
            String lastName = (String) updates.getOrDefault("lastName", "");
            String displayName = (firstName + " " + lastName).trim();

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();

            currentUser.updateProfile(profileUpdates);
        }

        // Update email if present
        if (updates.containsKey("email")) {
            String email = (String) updates.get("email");
            currentUser.updateEmail(email)
                    .addOnFailureListener(callback::onError);
        }

        if (updates.isEmpty()) {
            callback.onResult(false);
            return;
        }

        // Update password if present
        if (updates.containsKey("password")) {
            String password = (String) updates.get("password");
            currentUser.updatePassword(password)
                    .addOnFailureListener(callback::onError);
            updates.remove("password"); // avoid writing it to Firestore
        }


        // Update all entries that is present in the map in the firestore
        db.collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onResult(true))
                .addOnFailureListener(callback::onError);

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

    public void signInWithGoogle(AuthCredential credential, AuthCallback callback) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        callback.onSuccess(task.getResult().getUser());
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