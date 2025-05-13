package com.reserve.app;

import android.content.Context;
import android.content.SharedPreferences;

/*

    To access user details anywhere in the app:

    SessionManager sessionManager = new SessionManager(context);
    if (sessionManager.isLoggedIn()) {
        String userId = sessionManager.getUserId();
        String fullName = sessionManager.getFullName();
        String email = sessionManager.getEmail();
        String phone = sessionManager.getPhone();
        // Use these details for your transactions
    }

 */


public class SessionManager {
    private static final String PREF_NAME = "ReserveUserSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_AUTH_TYPE = "auth_type";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // User location keys
    private static final String KEY_LAT = "user_lat";
    private static final String KEY_LNG = "user_lng";

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveUserSession(String userId, String firstName, String lastName,
                                String email, String phone, String authType) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_AUTH_TYPE, authType);
        editor.apply();
    }

    public void saveUserLocation(double latitude, double longitude) {
        editor.putFloat(KEY_LAT, (float) latitude);
        editor.putFloat(KEY_LNG, (float) longitude);
        editor.apply();
    }

    public double getUserLat() {
        return prefs.getFloat(KEY_LAT, 0);
    }

    public double getUserLng() {
        return prefs.getFloat(KEY_LNG, 0);
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_USER_ID);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getFirstName() {
        return prefs.getString(KEY_FIRST_NAME, "");
    }

    public String getLastName() {
        return prefs.getString(KEY_LAST_NAME, "");
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public String getAuthType() {
        return prefs.getString(KEY_AUTH_TYPE, "");
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
