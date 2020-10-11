package de.kathrin.angebote;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.kathrin.angebote.adapter.ProductArrayAdapter;
import de.kathrin.angebote.alarm.AlarmHandler;
import de.kathrin.angebote.alarm.AlarmReceiver;
import de.kathrin.angebote.alarm.BootReceiver;
import de.kathrin.angebote.alarm.NotificationController;
import de.kathrin.angebote.database.MarketDataSource;
import de.kathrin.angebote.database.ProductDataSource;
import de.kathrin.angebote.models.Market;
import de.kathrin.angebote.models.Offer;
import de.kathrin.angebote.models.OfferList;
import de.kathrin.angebote.utlis.IOUtils;
import de.kathrin.angebote.utlis.LayoutUtilsNotification;
import de.kathrin.angebote.utlis.NotificationUtils;
import de.kathrin.angebote.utlis.OfferUtils;

/**
 * Activity to control the notifications
 */
public class NotificationActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + NotificationActivity.class.getSimpleName();

    private List<String> productList = new ArrayList<>();
    private ProductDataSource productDataSource;
    //private NotificationController notificationController;

    private LayoutUtilsNotification lu;

    /**
     * Called automatically when entering this the first time activity.
     * Sets the layout, initializes the Add-Product-Button and registers the alarm receiver.
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
        //initCheckProductsButton();

        // Connect to product notification database
        productDataSource = new ProductDataSource(this);

        // Connect to the notification controller
        //notificationController = new NotificationController(this);

        // Initialize list view
        bindAdapterToListView();

        // Set repeating alarm
        registerAlarmAndBootReceivers();
    }

    /**
     * Enables an AlarmReceiver and a BootReceiver in the PackageManager.
     * Both are used for notifications.
     */
    private void registerAlarmAndBootReceivers() {
        final PackageManager pm = NotificationActivity.this.getPackageManager();

        // Alarm Receiver
        pm.setComponentEnabledSetting(
                new ComponentName(NotificationActivity.this, AlarmReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        // Boot Receiver
        pm.setComponentEnabledSetting(
                new ComponentName(NotificationActivity.this, BootReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Add the elements in the text view to the list and to the database on button click
     */
    private void initAddProductButton () {
        View.OnClickListener onAddButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get product from Text View and reset it
                String product = lu.PRODUCT_ADD_FIELD_VIEW.getText().toString();
                lu.PRODUCT_ADD_FIELD_VIEW.setText("");

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
    /*private void initCheckProductsButton () {

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
    }*/

    /**
     * Connect the list view to the product array adapter
     */
    private void bindAdapterToListView () {
        lu.PRODUCT_LIST_VIEW.setAdapter(
                new ProductArrayAdapter(this, productList, productDataSource)
        );
    }

    /**
     * Notifies the user about the products on offer in a market
     * @param market    market which has the offers
     * @param offerList products on offer
     */
    /*private void notifyOffersAvailable (Market market, List<Offer> offerList) {
        Log.v(LOG_TAG, "Notify about available products on offer.");

        String title = "Neue Angebote im " + market.getName() + "!";

        StringBuilder content = new StringBuilder();
        for (Offer o: offerList) {
            content.append("- " + o.getTitle() + "\n");
        }

        notificationController.addNewNotification(title, content.toString());
    }*/

    /**
     * Called automatically every time entering this activity.
     * The database is opened and the current products to notify on offer are shown.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "On Resume");

        // Open database connection
        productDataSource.open();

        // Show all current products in the database
        productList.clear();
        productList.addAll(productDataSource.getAllProductsFromDatabase());
        lu.PRODUCT_LIST_VIEW.invalidateViews();

        //Log.v(LOG_TAG, "Loaded " + productList.size() + " elements from the database: " + productList.toString());
    }

    /**
     * Called automatically every time leaving this activity.
     * The database is closed and the alarm is set.
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "On Stop");

        // Close database connection
        productDataSource.close();

        // Set or clear alarm
        updateAlarm();
    }

    /**
     * Set or cancel the alarm.
     * If there are no products in the list, cancel it.
     * Otherwise check if there is already an alarm set. If not, set it.
     */
    private void updateAlarm () {

        if (productList.isEmpty()) {
            // Cancel alarm and delete file
            AlarmHandler.cancelAlarm(this);
            NotificationUtils.clearAlarmFile(this);
            Log.v(LOG_TAG, "List is empty, no alarm set.");

        } else if (NotificationUtils.alarmIsSet(this)){
            // When file has alarm: good, do nothing
            Log.v(LOG_TAG, "Alarm is already set.");

        } else {
            // Set alarm to next monday and update file
            Calendar nextMonday = NotificationUtils.getNextMonday();
            AlarmHandler.setAlarm(this, nextMonday);
            NotificationUtils.writeAlarmToFile(this, nextMonday);
            Log.v(LOG_TAG, "Setting new alarm to " + nextMonday.getTime());
        }
    }

    /*******************************************************************************************
     PRIVATE CLASS CHECK-OFFERS-TASKS
     1. Get the market and the product list
     2. Load the offers of the market from the server
     3. Go through all products and the offers to find matching once
     4. Return a list with all offers that match the product list.
     ********************************************************************************************/
//     private class CheckOffersTask extends AsyncTask <List<String>, String, List<Offer>> {
//        private Market market;
//
//        /**
//         * Constructor, saves the current market
//         * @param market    market which offers are compared later
//         */
//        public CheckOffersTask (Market market) {
//            this.market = market;
//        }
//
//        /**
//         * Load the offers from the given market and look for matching products (compare with
//         * the products in the given list).
//         * @param products  to compare with the offers in the market
//         * @return  list of matching offers
//         */
//        @Override
//        protected List<Offer> doInBackground(List<String>... products) {
//
//            List<Offer> resultList = new ArrayList<>();
//            OfferList offerList;
//
//            try {
//                offerList = OfferUtils.requestOffersFromServer(NotificationActivity.this, market);
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "IOException: " + e.getMessage());
//                return resultList;
//            }
//
//            // collect all matching offers
//            for (String p: products[0]) {
//
//                String product = p.trim().toLowerCase();
//
//                for (Offer o: offerList) {
//                    if (o.getTitle().toLowerCase().contains(product) ||
//                            o.getDescription().toLowerCase().contains(product)) {
//                        resultList.add(o);
//                    }
//                }
//            }
//
//            Log.v(LOG_TAG, "Results: " + resultList);
//
//            return resultList;
//        }
//
//        /**
//         * Notify the user about the products on offer.
//         * @param offers
//         */
//        @Override
//        protected void onPostExecute(List<Offer> offers) {
//            if (!offers.isEmpty()) {
//                notifyOffersAvailable(market, offers);
//            }
//        }
//    }
}
