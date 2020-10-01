package de.kathrin.angebote;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.kathrin.angebote.adapter.ProductArrayAdapter;
import de.kathrin.angebote.database.MarketDataSource;
import de.kathrin.angebote.database.ProductDataSource;
import de.kathrin.angebote.models.Market;
import de.kathrin.angebote.models.Offer;
import de.kathrin.angebote.models.OfferList;
import de.kathrin.angebote.utlis.LayoutUtilsNotification;
import de.kathrin.angebote.utlis.OfferUtils;

/**
 * Activity to control the notifications
 */
public class NotificationActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + NotificationActivity.class.getSimpleName();

    private List<String> productList = new ArrayList<>();
    private ProductDataSource productDataSource;
    private NotificationController notificationController;

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

        // Init the button to check if the product are on offer
        initCheckProductsButton();

        // Connect to product notification database
        productDataSource = new ProductDataSource(this);

        // Connect to the notification controller
        notificationController = new NotificationController(this);

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
     * Check if the products are on offer in the favourite markets.
     * (First load the markets from the market database, then start a {@link CheckOffersTask}
     * for every market.)
     */
    private void initCheckProductsButton () {

        // Define On Click Button Reaction
        View.OnClickListener onAddButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Check Button Clicked.");

                // Todo: Do this in the beginning once?
                MarketDataSource marketDataSource = new MarketDataSource(NotificationActivity.this);

                marketDataSource.open();
                List<Market> marketList = marketDataSource.getAllFavouriteMarkets();
                marketDataSource.close();

                if (marketList.isEmpty()) {
                    String message = "Sie haben keinen Lieblingsmarkt ausgewählt.";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }

                for (Market m: marketList) {
                    Log.v(LOG_TAG, "Checking market: " + m);
                    new CheckOffersTask(m).execute(productList);
                }

            }
        };

        // Add Reaction to Button
        lu.PRODUCT_CHECK_BUTTON_VIEW.setOnClickListener(onAddButtonClickListener);
    }

    /**
     * Connect the list view to the custom array adapter
     */
    private void bindAdapterToListView () {
        ProductArrayAdapter arrayAdapter = new ProductArrayAdapter(this, productList, productDataSource);
        lu.PRODUCT_LIST_VIEW.setAdapter(arrayAdapter);
    }

    /**
     * Notifies the user about the products on offer in a market
     * @param market    market which has the offers
     * @param offerList products on offer
     */
    private void notifyOffersAvailable (Market market, List<Offer> offerList) {
        Log.v(LOG_TAG, "Notify about available products on offer.");

        String title = "Neue Angebote im " + market.getName() + "!";

        StringBuilder content = new StringBuilder();
        for (Offer o: offerList) {
            content.append("- " + o.getTitle() + "\n");
        }

        notificationController.addNewNotification(title, content.toString());
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


    /*******************************************************************************************
     PRIVATE CLASS CHECK-OFFERS-TASKS
     1. Get the market and the product list
     2. Load the offers of the market from the server
     3. Go through all products and the offers to find matching once
     4. Return a list with all offers that match the product list.
     ********************************************************************************************/
    private class CheckOffersTask extends AsyncTask <List<String>, String, List<Offer>> {
        private Market market;

        /**
         * Constructor, saves the current market
         * @param market    market which offers are compared later
         */
        public CheckOffersTask (Market market) {
            this.market = market;
        }

        /**
         * Load the offers from the given market and look for matching products (compare with
         * the products in the given list).
         * @param products  to compare with the offers in the market
         * @return  list of matching offers
         */
        @Override
        protected List<Offer> doInBackground(List<String>... products) {

            List<Offer> resultList = new ArrayList<>();

            // TODO: First look in the files?
            OfferList offerList = OfferUtils.requestOffersFromServer(NotificationActivity.this, market);

            // collect all matching offers
            for (String p: products[0]) {

                String product = p.trim().toLowerCase();

                for (Offer o: offerList) {
                    if (o.getTitle().toLowerCase().contains(product) ||
                            o.getDescription().toLowerCase().contains(product)) {
                        resultList.add(o);
                    }
                }
            }

            Log.v(LOG_TAG, "Results: " + resultList);

            return resultList;
        }

        /**
         * Notify the user about the products on offer.
         * @param offers
         */
        @Override
        protected void onPostExecute(List<Offer> offers) {
            if (!offers.isEmpty()) {
                notifyOffersAvailable(market, offers);
            }
        }
    }
}
