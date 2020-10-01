package de.kathrin.angebote;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.kathrin.angebote.adapter.ProductArrayAdapter;
import de.kathrin.angebote.database.ProductDataSource;
import de.kathrin.angebote.utlis.LayoutUtilsNotification;

public class NotificationActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + NotificationActivity.class.getSimpleName();

    private List<String> productList = new ArrayList<>();
    private ProductDataSource productDataSource;

    private LayoutUtilsNotification lu;

    /**
     * Called automatically when entering this the first time activity.
     * Sets the layout.
     * @param savedInstanceState - save old state -
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "In Create");

        // Set Layout
        setContentView(LayoutUtilsNotification.NOTIFICATION_ACTIVITY);

        // Init Helper Class to handle the access to the layout elements
        lu = new LayoutUtilsNotification(this);

        // Init the button to add products to the list
        initAddProductButton();

        // Connect to product notification database
        productDataSource = new ProductDataSource(this);

        // Initialize list view
        bindAdapterToListView ();

    }

    /**
     * Add the elements in the text view to the list and database on the button click
     */
    private void initAddProductButton () {

        // Define On Click Button Reaction
        View.OnClickListener onAddButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get product from Text View
                String product = lu.PRODUCT_ADD_FIELD_VIEW.getText().toString();

                // Add product to list and database
                productList.add(product);
                productDataSource.addProductToNotificationDatabase(product);

                // Refresh the list view immediately
                lu.PRODUCT_LIST_VIEW.invalidateViews();
            }
        };

        // Add Reaction to Button
        lu.PRODUCT_ADD_BUTTON_VIEW.setOnClickListener(onAddButtonClickListener);

    }

    /**
     * Connect the list view to the custom array adapter
     */
    private void bindAdapterToListView () {
        ProductArrayAdapter arrayAdapter = new ProductArrayAdapter(this, productList, productDataSource);
        lu.PRODUCT_LIST_VIEW.setAdapter(arrayAdapter);
    }

    /**
     * Called automatically every time entering this activity.
     * The database is opened.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "On Resume");

        // Open database connection
        productDataSource.open();

        // Show all current products in the database
        productList.addAll(productDataSource.getAllProductsFromDatabase());
        lu.PRODUCT_LIST_VIEW.invalidateViews();

        Log.v(LOG_TAG, "Loaded " + productList.size() + " elements from the database: " +
                productList.toString());
    }

    /**
     * Called automatically every time leaving this activity.
     * The database is closed.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "On Pause");

        // Close database connection
        productDataSource.close();

    }
}
