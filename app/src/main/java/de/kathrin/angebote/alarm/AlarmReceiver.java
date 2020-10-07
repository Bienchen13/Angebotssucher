package de.kathrin.angebote.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.kathrin.angebote.MainActivity;
import de.kathrin.angebote.database.MarketDataSource;
import de.kathrin.angebote.database.ProductDataSource;
import de.kathrin.angebote.models.Market;
import de.kathrin.angebote.models.Offer;
import de.kathrin.angebote.models.OfferList;
import de.kathrin.angebote.utlis.NotificationUtils;
import de.kathrin.angebote.utlis.OfferUtils;

import static java.lang.Thread.sleep;

/**
 * Creates a new notification when the alarmManager send a signal
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + AlarmReceiver.class.getSimpleName();
    private boolean validInternetConnection = true;

    /**
     * Show a new notification. The method automatically is called, when an alarm
     * (from the alarmManager, set in {@link AlarmHandler}) is received
     * @param context   current context
     * @param intent    not used
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "Alarm received.");

        checkForNewOffers(context);

        Log.v(LOG_TAG, "After here.");

        try {
            // Sleep for 20 sec
            sleep(20*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.v(LOG_TAG, "And now Im here");
        setNewAlarm(context);
    }

    /**
     * Set the next alarm. In one week if every thing was fine. In one hour if there was no
     * internet connection
     * @param context   current context
     */
    private void setNewAlarm(Context context) {

        Calendar date = Calendar.getInstance();

        if (validInternetConnection) {
            // Set Date on next Monday 6h
            date.set(Calendar.DAY_OF_WEEK, 2);
            date.set(Calendar.HOUR_OF_DAY, 9);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.add(Calendar.DATE, 7);
        } else {
            // Set date on in one hour
            date.add(Calendar.HOUR_OF_DAY, 1);
        }

        // Set alarm to date
        AlarmHandler.setAlarm(context, date);

        // Update file
        NotificationUtils.writeAlarmToFile(context, date);

        Log.v(LOG_TAG, "Setting new alarm to " + date.getTime());
    }

    /**
     * Check if the products are on offer in the favourite markets.
     * (First load the markets from the market database, then start a {@link CheckOffersTask}
     *  for every market.)
     * @param context   current context
     */
    private void checkForNewOffers (Context context) {

        List<String> productList = new ArrayList<>();

        ProductDataSource productDataSource = new ProductDataSource(context);

        productDataSource.open();
        productList.addAll(productDataSource.getAllProductsFromDatabase());
        productDataSource.close();

        MarketDataSource marketDataSource = new MarketDataSource(context);

        marketDataSource.open();
        List<Market> marketList = marketDataSource.getAllFavouriteMarkets();
        marketDataSource.close();

        if (!marketList.isEmpty()) {

            for (Market m : marketList) {
                Log.v(LOG_TAG, "Checking market: " + m);
                new CheckOffersTask(context, m).execute(productList);
            }
        }

    }

    /*******************************************************************************************
     PRIVATE CLASS CHECK-OFFERS-TASKS
     1. Get the market and the product list
     2. Load the offers of the market from the server
     3. Go through all products and the offers to find matching once
     4. Return a list with all offers that match the product list.
     ********************************************************************************************/
    private class CheckOffersTask extends AsyncTask<List<String>, String, List<Offer>>{
        private Market market;
        private Context context;

        /**
         * Constructor, saves the current market
         * @param market    market which offers are compared later
         */
        public CheckOffersTask (Context context, Market market) {
            this.context = context;
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

            // TODO: First look in the files

            OfferList offerList = null;
            try {
                offerList = OfferUtils.requestOffersFromServer(context, market);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException: " + e.getMessage());
                e.printStackTrace();
                validInternetConnection = false;
                return resultList;
            }

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
                NotificationController nc = new NotificationController(context);

                String title = "Neue Angebote im " + market.getName() + "!";

                StringBuilder content = new StringBuilder();
                for (Offer o: offers) {
                    content.append("- " + o.getTitle() + "\n");
                }

                nc.addNewNotification(title, content.toString());
            }
        }
    }
}
