package com.reserve.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "ReserveAppDB";

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_PARKING_SPACES = "parking_spaces";
    private static final String TABLE_RENTALS = "rentals";
    private static final String TABLE_MESSAGES = "messages";

    // Common Column Names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // Users Table Columns
    private static final String USER_FIRST_NAME = "first_name";
    private static final String USER_LAST_NAME = "last_name";
    private static final String USER_EMAIL = "email";
    private static final String USER_PASSWORD = "password";
    private static final String USER_PHONE = "phone";
    private static final String USER_AUTH_TYPE = "auth_type";

    // Parking Space Table Column
    private static final String PARKING_SPACE_NAME = "name";
    private static final String PARKING_SPACE_LOCATION = "location";
    private static final String PARKING_3H_RATE = "3h_rate";
    private static final String PARKING_6H_RATE = "6h_rate";
    private static final String PARKING_12H_RATE = "12h_rate";
    private static final String PARKING_24H_RATE = "24h_rate";
    private static final String PARKING_USER_ID = "user_id";

    // Rental Table Column
    private static final String RENTAL_USER_ID = "user_id";
    private static final String RENTAL_PARKING_SPACE_ID = "parking_space_id";
    private static final String RENTAL_START_TIME = "start_time";
    private static final String RENTAL_END_TIME = "end_time";
    private static final String RENTAL_TOTAL_COST = "total_cost";
    private static final String RENTAL_STATUS = "status";

    // Messages Table Column
    private static final String MESSAGE_SENDER_ID = "sender_id";
    private static final String MESSAGE_RECEIVER_ID = "receiver_id";
    private static final String MESSAGE_CONTENT = "content";
    private static final String MESSAGE_TIMESTAMP = "timestamp";

    // Singleton Instance
    private static DatabaseHandler instance;

    public DatabaseHandler(Context context){
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHandler getInstance(Context context){
        if (instance == null){
            instance = new DatabaseHandler(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createParkingSpacesTable(db);
        createRentalsTable(db);
        createMessagesTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING_SPACES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RENTALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);

        // Create tables again
        onCreate(db);
    }

    private void createUsersTable(SQLiteDatabase db){
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + USER_FIRST_NAME + " TEXT,"
                + USER_LAST_NAME + " TEXT,"
                + USER_EMAIL + " TEXT UNIQUE,"
                + USER_PASSWORD + " TEXT,"
                + USER_PHONE + " TEXT,"
                + USER_AUTH_TYPE + " TEXT,"
                + KEY_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    private void createParkingSpacesTable(SQLiteDatabase db){
        String CREATE_PARKING_SPACES_TABLE = "CREATE TABLE " + TABLE_PARKING_SPACES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PARKING_SPACE_NAME + " TEXT,"
                + PARKING_SPACE_LOCATION + " TEXT,"
                + PARKING_3H_RATE + " REAL,"
                + PARKING_6H_RATE + " REAL,"
                + PARKING_12H_RATE + " REAL,"
                + PARKING_24H_RATE + " REAL,"
                + PARKING_USER_ID + " INTEGER,"
                + KEY_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_PARKING_SPACES_TABLE);
    }

    private void createRentalsTable(SQLiteDatabase db){
        String CREATE_RENTALS_TABLE = "CREATE TABLE " + TABLE_RENTALS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RENTAL_USER_ID + " INTEGER,"
                + RENTAL_PARKING_SPACE_ID + " INTEGER,"
                + RENTAL_START_TIME + " TIMESTAMP,"
                + RENTAL_END_TIME + " TIMESTAMP,"
                + RENTAL_TOTAL_COST + " REAL,"
                + RENTAL_STATUS + " TEXT,"
                + KEY_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_RENTALS_TABLE);
    }

    private void createMessagesTable(SQLiteDatabase db){
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MESSAGE_SENDER_ID + " INTEGER,"
                + MESSAGE_RECEIVER_ID + " INTEGER,"
                + MESSAGE_CONTENT + " TEXT,"
                + MESSAGE_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    public long createUser(String firstName, String lastName, String email, String password, String phone, String authType) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_FIRST_NAME, firstName);
        values.put(USER_LAST_NAME, lastName);
        values.put(USER_EMAIL, email);
        values.put(USER_PASSWORD, password);
        values.put(USER_PHONE, phone);
        values.put(USER_AUTH_TYPE, authType);

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public long createParkingSpace(String name, String location, double rate3h, double rate6h, double rate12h, double rate24h, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PARKING_SPACE_NAME, name);
        values.put(PARKING_SPACE_LOCATION, location);
        values.put(PARKING_3H_RATE, rate3h);
        values.put(PARKING_6H_RATE, rate6h);
        values.put(PARKING_12H_RATE, rate12h);
        values.put(PARKING_24H_RATE, rate24h);
        values.put(PARKING_USER_ID, userId);

        long id = db.insert(TABLE_PARKING_SPACES, null, values);
        db.close();
        return id;
    }

    public long createRental(int userId, int parkingSpaceId, String startTime, String endTime, double totalCost, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RENTAL_USER_ID, userId);
        values.put(RENTAL_PARKING_SPACE_ID, parkingSpaceId);
        values.put(RENTAL_START_TIME, startTime);
        values.put(RENTAL_END_TIME, endTime);
        values.put(RENTAL_TOTAL_COST, totalCost);
        values.put(RENTAL_STATUS, status);

        long id = db.insert(TABLE_RENTALS, null, values);
        db.close();
        return id;
    }

    public long createMessage(int senderId, int receiverId, String content) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MESSAGE_SENDER_ID, senderId);
        values.put(MESSAGE_RECEIVER_ID, receiverId);
        values.put(MESSAGE_CONTENT, content);

        long id = db.insert(TABLE_MESSAGES, null, values);
        db.close();
        return id;
    }

    public long readUserDetails(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        long id = -1;

        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + USER_EMAIL + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(KEY_ID);
            if (columnIndex != -1) {
                id = cursor.getLong(columnIndex);
            }
            cursor.close();
        }
        db.close();
        return id;
    }

    public long readParkingSpaceDetails(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        long id = -1;

        String query = "SELECT * FROM " + TABLE_PARKING_SPACES + " WHERE " + PARKING_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(KEY_ID);
            if (columnIndex != -1) {
                id = cursor.getLong(columnIndex);
            }
            cursor.close();
        }
        db.close();
        return id;
    }

    public long readRentalDetails(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        long id = -1;

        String query = "SELECT * FROM " + TABLE_RENTALS + " WHERE " + RENTAL_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(KEY_ID);
            if (columnIndex != -1) {
                id = cursor.getLong(columnIndex);
            }
            cursor.close();
        }
        db.close();
        return id;
    }

    public long updateUserDetails(int userId, String firstName, String lastName, String email, String password, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_FIRST_NAME, firstName);
        values.put(USER_LAST_NAME, lastName);
        values.put(USER_EMAIL, email);
        values.put(USER_PASSWORD, password);
        values.put(USER_PHONE, phone);

        int rowsAffected = db.update(TABLE_USERS, values, KEY_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected;
    }

    public long updateParkingSpaceDetails(int parkingSpaceId, String name, String location, double rate3h, double rate6h, double rate12h, double rate24h) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PARKING_SPACE_NAME, name);
        values.put(PARKING_SPACE_LOCATION, location);
        values.put(PARKING_3H_RATE, rate3h);
        values.put(PARKING_6H_RATE, rate6h);
        values.put(PARKING_12H_RATE, rate12h);
        values.put(PARKING_24H_RATE, rate24h);

        int rowsAffected = db.update(TABLE_PARKING_SPACES, values, KEY_ID + "=?", new String[]{String.valueOf(parkingSpaceId)});
        db.close();
        return rowsAffected;
    }

    public boolean isEmailUnique(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + USER_EMAIL + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        boolean isUnique = (cursor.getCount() == 0);
        cursor.close();
        db.close();
        return isUnique;
    }

    public boolean isPhoneUnique(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + USER_PHONE + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{phone});

        boolean isUnique = (cursor.getCount() == 0);
        cursor.close();
        db.close();
        return isUnique;
    }
}
