package de.kathrin.angebote.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.kathrin.angebote.MainActivity;
import de.kathrin.angebote.database.MarketDataSource;
import de.kathrin.angebote.database.ProductDataSource;
import de.kathrin.angebote.models.Market;
import de.kathrin.angebote.models.Offer;
import de.kathrin.angebote.models.OfferList;
import de.kathrin.angebote.utlis.OfferUtils;

/**
 * Creates a new notification when the alarmManager send a signal
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + AlarmReceiver.class.getSimpleName();

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
    private class CheckOffersTask extends AsyncTask<List<String>, String, List<Offer>> {
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
            OfferList offerList = OfferUtils.requestOffersFromServer(context, market);

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
