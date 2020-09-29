package de.kathrin.angebote.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import de.kathrin.angebote.MainActivity;

/**
 * Class to create and handle the market database where all favourite markets are saved.
 */
public class MarketDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + MarketDbHelper.class.getSimpleName();

    public static final String DB_NAME = "market_favourites.db";
    public static final int DB_VERSION = 1;

    // Set all column names
    public static final String TABLE_MARKET_FAVOURITES  = "market_favourites";
    public static final String COLUMN_ID                = "_id";
    public static final String COLUMN_MARKET_ID         = "marketid";
    public static final String COLUMN_NAME              = "name";
    public static final String COLUMN_STREET            = "street";
    public static final String COLUMN_CITY              = "city";
    public static final String COLUMN_PLZ               = "plz";

    // Set the instruction to create the favourite markets table
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


    /**
     * Constructor using the constructor of the Java Class {@link SQLiteOpenHelper},
     * giving it the name and the version of the database
     * @param context needed by SQLiteOpenHelper
     */
    public MarketDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Called automatically when an instance of this class is created, creates the table
     * for the favourite markets
     * @param db - done by Java -
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.v(LOG_TAG, "Creating table with SQL command: " + SQL_CREATE);
            db.execSQL(SQL_CREATE);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error creating table: " + e.getMessage());
        }
    }

    /**
     * Not used
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
