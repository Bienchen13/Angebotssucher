package de.kathrin.angebote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MarketDataSource {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + MarketDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private MarketDbHelper dbHelper;
    private String[] columns = {
            MarketDbHelper.COLUMN_ID,
            MarketDbHelper.COLUMN_MARKET_ID,
            MarketDbHelper.COLUMN_NAME,
            MarketDbHelper.COLUMN_STREET,
            MarketDbHelper.COLUMN_CITY,
            MarketDbHelper.COLUMN_PLZ
    };

    public MarketDataSource(Context context) {
        Log.v(LOG_TAG, "DbHelper is created.");
        dbHelper = new MarketDbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
        Log.v(LOG_TAG, "Opened database connection.");
    }

    public void close () {
        dbHelper.close();
        Log.v(LOG_TAG,"Closed database connection.");
    }

    public void addMarketToFavourites (Market m) {
        ContentValues values = new ContentValues();
        values.put(MarketDbHelper.COLUMN_MARKET_ID, m.getMarketID());
        values.put(MarketDbHelper.COLUMN_NAME, m.getName());
        values.put(MarketDbHelper.COLUMN_STREET, m.getStreet());
        values.put(MarketDbHelper.COLUMN_CITY, m.getCity());
        values.put(MarketDbHelper.COLUMN_PLZ, m.getPlz());
        long insertID = database.insert(MarketDbHelper.TABLE_MARKET_FAVOURITES,
                null, values);

        Log.v(LOG_TAG, "Added favourite market! ID: " + insertID + " Market: " + m.toString());
    }

    public void deleteMarketFromFavourites (Market m) {
        String id = m.getMarketID();

        database.delete(MarketDbHelper.TABLE_MARKET_FAVOURITES,
                MarketDbHelper.COLUMN_MARKET_ID + "=" + id,
                null);

        Log.v(LOG_TAG, "Market deleted! Market: " + m.toString());
    }

    private Market cursorToMarket (Cursor cursor) {
        int idIndex = cursor.getColumnIndex(MarketDbHelper.COLUMN_ID);
        int idMarketId = cursor.getColumnIndex(MarketDbHelper.COLUMN_MARKET_ID);
        int idName = cursor.getColumnIndex(MarketDbHelper.COLUMN_NAME);
        int idStreet = cursor.getColumnIndex(MarketDbHelper.COLUMN_STREET);
        int idCity = cursor.getColumnIndex(MarketDbHelper.COLUMN_CITY);
        int idPlz = cursor.getColumnIndex(MarketDbHelper.COLUMN_PLZ);

        String marketId = cursor.getString(idMarketId);
        String name = cursor.getString(idName);
        String street = cursor.getString(idStreet);
        String city = cursor.getString(idCity);
        String plz = cursor.getString(idPlz);
        long id = cursor.getLong(idIndex);

        return new Market(marketId, name, street, city, plz, id);
    }

    public List<Market> getAllFavouriteMarkets() {
        List<Market> marketList = new ArrayList<>();

        Cursor cursor = database.query(MarketDbHelper.TABLE_MARKET_FAVOURITES, columns,
                null, null, null, null, null);

        cursor.moveToFirst();
        Market market;

        while (!cursor.isAfterLast()) {
            market = cursorToMarket(cursor);
            marketList.add(market);
            Log.v(LOG_TAG, "ID: " + market.get_id() + ", Market: " + market.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return marketList;
    }

    public boolean checkMarketInFavourites (Market m) {
        Cursor cursor = database.query(MarketDbHelper.TABLE_MARKET_FAVOURITES, columns,
                MarketDbHelper.COLUMN_MARKET_ID + "=" + m.getMarketID(),
                null, null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() == 0) {
            return false;
        } else {
            return true;
        }
    }


}
