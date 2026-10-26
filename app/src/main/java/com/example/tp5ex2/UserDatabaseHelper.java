package com.example.tp5ex2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    // Constantes pour la base de données
    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 3; // Augmenter la version

    // Nom de la table des utilisateurs
    private static final String TABLE_USERS = "users";

    // Colonnes de la table users
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_SALT = "salt";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Tag pour les logs
    private static final String TAG = "UserDatabaseHelper";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Requête SQL pour créer la table users
            String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, "
                    + COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, "
                    + COLUMN_PASSWORD + " TEXT NOT NULL, "
                    + COLUMN_SALT + " TEXT, "
                    + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

            // Exécution de la requête
            db.execSQL(CREATE_USERS_TABLE);
            Log.d(TAG, "Table users créée avec succès");
        } catch (Exception e) {
            Log.e(TAG, "Error creating table: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // Suppression de l'ancienne table
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            Log.d(TAG, "Ancienne table users supprimée");

            // Recréation de la table
            onCreate(db);
            Log.d(TAG, "Nouvelle table users créée");
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage());
        }
    }






}