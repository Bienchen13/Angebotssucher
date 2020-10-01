package de.kathrin.angebote.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.kathrin.angebote.MainActivity;

/**
 * Handling the Product Data Source where all product notifications are saved.
 * (Using the {@link DbHelper} class)
 */
public class ProductDataSource {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + ProductDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private String[] columns = {
            DbHelper.PRODUCT_COLUMN_ID,
            DbHelper.PRODUCT_COLUMN_PRODUCT
    };

    /**
     * Constructor creating a {@link DbHelper} instance
     * @param context given to the ProductDbHelper instance
     */
    public ProductDataSource(Context context) {
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
     * @param product product added to the notification database
     */
    public void addProductToNotificationDatabase (String product) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.PRODUCT_COLUMN_PRODUCT, product);
        long insertID = database.insert(DbHelper.TABLE_PRODUCT_NOTIFICATION,
                null, values);

        Log.v(LOG_TAG, "Added product " + product +  "! ID: " + insertID);
    }

    /**
     * Delete the given product from the database
     * @param product product deleted from the notification database
     */
    public void deleteProductFromNotificationDatabase (String product) {
        Log.v(LOG_TAG, "Trying to delete: " + product);

        database.delete(DbHelper.TABLE_PRODUCT_NOTIFICATION,
                DbHelper.PRODUCT_COLUMN_PRODUCT + "=\"" + product + "\"",
                null);

        Log.v(LOG_TAG, "Product "+ product + " deleted!");
    }

    /**
     * Return a list with all markets in the database.
     * (Walking through the entire database saving all markets in one list.)
     * @return a list with all markets in the database
     */
    public List<String> getAllProductsFromDatabase() {
        List<String> productList = new ArrayList<>();

        // Get entire database
        Cursor cursor = database.query(DbHelper.TABLE_PRODUCT_NOTIFICATION, columns,
                null, null, null, null, null);

        cursor.moveToFirst();

        // Step through very entry and add it to the list
        while (!cursor.isAfterLast()) {
            String product = cursor.getString(cursor.getColumnIndex(DbHelper.PRODUCT_COLUMN_PRODUCT));
            productList.add(product);
            cursor.moveToNext();
        }

        cursor.close();

        return productList;
    }
}
