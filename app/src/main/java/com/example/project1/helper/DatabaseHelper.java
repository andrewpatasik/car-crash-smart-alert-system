package com.example.project1.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.project1.DataTabrakan.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "DATABASE_HELPER";
    public static final String DATABASE_NAME = "testdatatabrakan.db";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_DATATABRAKAN_TABLE = "CREATE TABLE " +
                TabrakanEntry.TABLE_NAME + " (" +
                TabrakanEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TabrakanEntry.COLUMN_DRIVER + " TEXT NOT NULL, " +
                TabrakanEntry.COLUMN_CAR + " TEXT NOT NULL, " +
                TabrakanEntry.COLUMN_X + " TEXT NOT NULL, " +
                TabrakanEntry.COLUMN_Y + " TEXT NOT NULL, " +
                TabrakanEntry.COLUMN_LAT + " DOUBLE NOT NULL, " +
                TabrakanEntry.COLUMN_LONG + " DOUBLE NOT NULL, " +
                TabrakanEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        db.execSQL(SQL_CREATE_DATATABRAKAN_TABLE);
        Log.d(TAG,"SQLite: " + SQL_CREATE_DATATABRAKAN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TabrakanEntry.TABLE_NAME);
        onCreate(db);
    }
}