package de.kathrin.angebote.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.kathrin.angebote.models.Market;

import static de.kathrin.angebote.utlis.Strings.PROJECT_NAME;

/**
 * Handling the Market Data Source where all favourite markets are saved.
 * (Using the {@link DbHelper} class)
 */
public class MarketDataSource {

    private static final String LOG_TAG = PROJECT_NAME + MarketDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private String[] columns = {
            DbHelper.MARKET_COLUMN_ID,
            DbHelper.MARKET_COLUMN_MARKET_ID,
            DbHelper.MARKET_COLUMN_NAME,
            DbHelper.MARKET_COLUMN_STREET,
            DbHelper.MARKET_COLUMN_CITY,
            DbHelper.MARKET_COLUMN_PLZ
    };

    /**
     * Constructor creating a {@link DbHelper} instance
     * @param context given to the MarketDbHelper instance
     */
    public MarketDataSource(Context context) {
        Log.v(LOG_TAG, "DbHelper is created.");
        dbHelper = new DbHelper(context);
    }

    /**
     * Opens a new database connection
     */
    public void open() {
        database = dbHelper.getWritableDatabase();
        Log.v(LOG_TAG, "Opened database connection.");
    }

    /**
     * Closes the current database connection
     */
    public void close () {
        dbHelper.close();
        Log.v(LOG_TAG,"Closed database connection.");
    }

    /**
     * Add the given market to the database
     * @param m market added to the favourites
     */
    public void addMarketToFavourites (Market m) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.MARKET_COLUMN_MARKET_ID, m.getMarketID());
        values.put(DbHelper.MARKET_COLUMN_NAME, m.getName());
        values.put(DbHelper.MARKET_COLUMN_STREET, m.getStreet());
        values.put(DbHelper.MARKET_COLUMN_CITY, m.getCity());
        values.put(DbHelper.MARKET_COLUMN_PLZ, m.getPlz());
        long insertID = database.insert(DbHelper.TABLE_MARKET_FAVOURITES,
                null, values);

        Log.v(LOG_TAG, "Added favourite market! ID: " + insertID + " Market: " + m.toString());
    }

    /**
     * Delete the given market from the database
     * @param m market deleted from the favourites
     */
    public void deleteMarketFromFavourites (Market m) {
        String id = m.getMarketID();

        database.delete(DbHelper.TABLE_MARKET_FAVOURITES,
                DbHelper.MARKET_COLUMN_MARKET_ID + "=" + id,
                null);

        Log.v(LOG_TAG, "Market deleted! Market: " + m.toString());
    }

    /**
     * Private helper method that converts the cursor data in the database to a market instance
     * @param cursor  current database position
     * @return the market filled with the data of the cursor position
     */
    private Market cursorToMarket (Cursor cursor) {
        String marketId = cursor.getString(cursor.getColumnIndex(DbHelper.MARKET_COLUMN_MARKET_ID));
        String name     = cursor.getString(cursor.getColumnIndex(DbHelper.MARKET_COLUMN_NAME));
        String street   = cursor.getString(cursor.getColumnIndex(DbHelper.MARKET_COLUMN_STREET));
        String city     = cursor.getString(cursor.getColumnIndex(DbHelper.MARKET_COLUMN_CITY));
        String plz      = cursor.getString(cursor.getColumnIndex(DbHelper.MARKET_COLUMN_PLZ));
        long id         = cursor.getLong(cursor.getColumnIndex(DbHelper.MARKET_COLUMN_ID));

        return new Market(marketId, name, street, city, plz, id);
    }

    /**
     * Return a list with all markets in the database.
     * (Walking through the entire database saving all markets in one list.)
     * @return a list with all markets in the database
     */
    public List<Market> getAllFavouriteMarkets() {
        List<Market> marketList = new ArrayList<>();

        // Get entire database
        Cursor cursor = database.query(DbHelper.TABLE_MARKET_FAVOURITES, columns,
                null, null, null, null, null);

        cursor.moveToFirst();
        Market market;

        // Step through very entry and add it to the list
        while (!cursor.isAfterLast()) {
            market = cursorToMarket(cursor);
            marketList.add(market);
            cursor.moveToNext();

            Log.v(LOG_TAG, "ID: " + market.get_id() + ", Market: " + market.toString());
        }

        cursor.close();

        return marketList;
    }

    /**
     * Check if a market is a favourite market.
     * (If it is int the database
     * @param m market that is checked
     * @return  boolean, true if market is favourite
     */
    public boolean checkMarketInFavourites (Market m) {
        // Go to market in datbase
        Cursor cursor = database.query(DbHelper.TABLE_MARKET_FAVOURITES, columns,
                DbHelper.MARKET_COLUMN_MARKET_ID + "=" + m.getMarketID(),
                null, null, null, null);
        cursor.moveToFirst();
        cursor.close();

        // There is no entry found, return false, else true
        return cursor.getCount() != 0;
    }


}
