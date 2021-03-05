package de.kathrin.angebote.alarm;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.kathrin.angebote.database.MarketDataSource;
import de.kathrin.angebote.database.ProductDataSource;
import de.kathrin.angebote.models.Market;
import de.kathrin.angebote.models.Offer;
import de.kathrin.angebote.models.OfferList;
import de.kathrin.angebote.utlis.NotificationUtils;
import de.kathrin.angebote.utlis.OfferUtils;

import static de.kathrin.angebote.utlis.Strings.PROJECT_NAME;
import static java.lang.Thread.sleep;

/**
 * Creates a new notification when the alarmManager sends a signal
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = PROJECT_NAME + AlarmReceiver.class.getSimpleName();
    private boolean validInternetConnection = true;
    private Context context;

    /**
     * Show a new notification. The method automatically is called, when an alarm
     * (from the alarmManager, set in {@link AlarmHandler}) is received
     * @param context   current context
     * @param intent    not used
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "Alarm received.");
        this.context = context;
        // Only show notifications on monday
        // -> check if today is monday and only check for new offers then
        // if it's not monday, set an alarm to check again the next day
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        if (today != Calendar.MONDAY) {
            setNewAlarm();
        } else {
            // Check if there are new offers and send notifications if there are
            // Set the new alarm depending on the success of the search
            checkForNewOffers(true);
        }
    }

    /**
     * Set the next alarm. Tomorrow if everything was fine. In one hour if there was no
     * internet connection
     */
    private void setNewAlarm() {

        // Set date for tomorrow
        Calendar date = NotificationUtils.getTomorrow();

        // If there was no connection, change it to: in one hour
        if (!validInternetConnection) {
            // Set date on in one hour
            date =  Calendar.getInstance();
            date.add(Calendar.HOUR_OF_DAY, 1);
        }

        // Set alarm to date and update file
        AlarmHandler.setAlarm(context, date);
        NotificationUtils.writeAlarmToFile(context, date);

        Log.v(LOG_TAG, "Setting new alarm to " + date.getTime());
    }

    /**
     * Check if the products are on offer in the favourite markets.
     * (First load the markets from the market database, then start a {@link CheckOffersTask}
     *  for every market.)
     * @param setNewAlarm boolean indicating whether to set an alarm after the check completes
     */
    @SuppressWarnings("unchecked")
    private void checkForNewOffers (boolean setNewAlarm) {

        // Load all products of interest
        ProductDataSource productDataSource = new ProductDataSource(context);
        productDataSource.open();
        List<String> productList = productDataSource.getAllProductsFromDatabase();
        productDataSource.close();

        // Load all favourite markets
        MarketDataSource marketDataSource = new MarketDataSource(context);
        marketDataSource.open();
        List<Market> marketList = marketDataSource.getAllFavouriteMarkets();
        marketDataSource.close();

        if (!marketList.isEmpty()) {

            // Go through all markets and check for products on offer
            int i = 0;
            for (Market m : marketList) {
                Log.v(LOG_TAG, "Checking market: " + m);
                // Only set the alarm once, in the first iteration
                boolean setAlarm = setNewAlarm && i==0;
                new CheckOffersTask(context, m, setAlarm).execute(productList);
                i++;
            }
        }

    }

    /**
     * Create the title and the content of a new notification and send it to the
     * {@link NotificationController}.
     * @param market    Market where the products are on offer
     * @param offers    Products on offer.
     */
    private void notifyAboutOffers (Market market, List<Offer> offers) {
        NotificationController nc = new NotificationController(context);

        String title = "Neue Angebote im " + market.getName() + "!";

        StringBuilder content = new StringBuilder();
        for (Offer o: offers) {
            content.append("- ")
                    .append(o.getTitle())
                    .append("\n");
        }

        // Show new notification
        nc.addNewNotification(title, content.toString());
    }

    /*******************************************************************************************
     PRIVATE CLASS CHECK-OFFERS-TASKS
     1. Get the market and the product list
     2. Load the offers of the market from the server
     3. Go through all products and the offers to find matching once
     4. Return a list with all offers that match the product list.
     ********************************************************************************************/
    @SuppressLint("StaticFieldLeak")
    private class CheckOffersTask extends AsyncTask<List<String>, String, List<Offer>>{
        private final Market market;
        private final Context context;
        private final boolean setAlarm;

        /**
         * Constructor, saves the current market
         * @param market    market which offers are compared later
         * @param setAlarm  whether to set an alarm after the task completes
         */
        public CheckOffersTask (Context context, Market market, boolean setAlarm) {
            this.context = context;
            this.market = market;
            this.setAlarm = setAlarm;
        }

        /**
         * Load the offers from the given market and look for matching products (compare with
         * the products in the given list).
         * @param products  to compare with the offers in the market
         * @return  list of matching offers
         */
        @SafeVarargs
        @Override
        protected final List<Offer> doInBackground(List<String>... products) {

            List<Offer> resultList = new ArrayList<>();
            OfferList offerList;

            // Try to load the offers from the server
            try {
                offerList = OfferUtils.requestOffersFromServer(context, market);
            } catch (IOException e) {
                Log.v(LOG_TAG, "No valid internet connection");
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

            //Log.v(LOG_TAG, "Results: " + resultList);
            return resultList;
        }

        /**
         * Notify the user about the products on offer.
         * @param offers    products on offer
         */
        @Override
        protected void onPostExecute(List<Offer> offers) {
            if (this.setAlarm) {
                setNewAlarm();
            }
            if (!offers.isEmpty()) {
                notifyAboutOffers(market, offers);
            }
        }
    }
}
