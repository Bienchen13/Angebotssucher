package de.kathrin.angebote.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import de.kathrin.angebote.MainActivity;

/**
 * Helper Class to create the favourite-markets and the product-notification databases
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + DbHelper.class.getSimpleName();

    public static final String DB_NAME = "angebote.db";
    public static final int DB_VERSION = 1;

    // Set all column names for the market table
    public static final String TABLE_MARKET_FAVOURITES  = "market_favourites";
    public static final String MARKET_COLUMN_ID         = "_id";
    public static final String MARKET_COLUMN_MARKET_ID  = "marketid";
    public static final String MARKET_COLUMN_NAME       = "name";
    public static final String MARKET_COLUMN_STREET     = "street";
    public static final String MARKET_COLUMN_CITY       = "city";
    public static final String MARKET_COLUMN_PLZ        = "plz";

    // Set all column names for the product table
    public static final String TABLE_PRODUCT_NOTIFICATION   = "product_notification";
    public static final String PRODUCT_COLUMN_ID            = "_id";
    public static final String PRODUCT_COLUMN_PRODUCT       = "product";

    // Set the instruction to create the favourite markets table
    public static final String SQL_CREATE_MARKET =
            "CREATE TABLE " + TABLE_MARKET_FAVOURITES +
                    " (" +
                    MARKET_COLUMN_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MARKET_COLUMN_MARKET_ID + " TEXT NOT NULL, " +
                    MARKET_COLUMN_NAME      + " TEXT NOT NULL, " +
                    MARKET_COLUMN_STREET    + " TEXT NOT NULL, " +
                    MARKET_COLUMN_CITY      + " TEXT NOT NULL, " +
                    MARKET_COLUMN_PLZ       + " TEXT NOT NULL"   +
                    ");";

    // Set the instruction to create the product notification table
    public static final String SQL_CREATE_PRODUCT =
            "CREATE TABLE " + TABLE_PRODUCT_NOTIFICATION +
                    " (" +
                    PRODUCT_COLUMN_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PRODUCT_COLUMN_PRODUCT   + " TEXT NOT NULL " +
                    ");";


    /**
     * Constructor using the constructor of the Java Class {@link SQLiteOpenHelper},
     * giving it the name and the version of the database
     * @param context needed by SQLiteOpenHelper
     */
    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Called automatically when an instance of this class is created, creates the tables
     * for the favourite markets and the product notifications
     * @param db - done by Java -
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.v(LOG_TAG, "Creating table with SQL command: " + SQL_CREATE_MARKET);
            db.execSQL(SQL_CREATE_MARKET);

            Log.v(LOG_TAG, "Creating table with SQL command: " + SQL_CREATE_PRODUCT);
            db.execSQL(SQL_CREATE_PRODUCT);
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
