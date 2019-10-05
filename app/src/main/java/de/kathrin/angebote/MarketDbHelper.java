package de.kathrin.angebote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MarketDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + MarketDbHelper.class.getSimpleName();

    public static final String DB_NAME = "market_favourites.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_MARKET_FAVOURITES  = "market_favourites";
    public static final String COLUMN_ID                = "_id";
    public static final String COLUMN_MARKET_ID         = "marketid";
    public static final String COLUMN_NAME              = "name";
    public static final String COLUMN_STREET            = "street";
    public static final String COLUMN_CITY              = "city";
    public static final String COLUMN_PLZ               = "plz";

    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_MARKET_FAVOURITES +
                    " (" +
                    COLUMN_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MARKET_ID + " TEXT NOT NULL, " +
                    COLUMN_NAME      + " TEXT NOT NULL, " +
                    COLUMN_STREET    + " TEXT NOT NULL, " +
                    COLUMN_CITY      + " TEXT NOT NULL, " +
                    COLUMN_PLZ       + " TEXT NOT NULL"   +
                    ");";


    public MarketDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.v(LOG_TAG, "Creating table with SQL command: " + SQL_CREATE);
            db.execSQL(SQL_CREATE);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error creating table: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
