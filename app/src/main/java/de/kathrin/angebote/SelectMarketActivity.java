package de.kathrin.angebote;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.kathrin.angebote.adapter.MarketArrayAdapter;
import de.kathrin.angebote.database.MarketDataSource;
import de.kathrin.angebote.models.Market;
import de.kathrin.angebote.utlis.LayoutUtilsMain;
import de.kathrin.angebote.utlis.LayoutUtilsSelectMarket;
import de.kathrin.angebote.utlis.MarketUtils;

/**
 * Activity to change the selected market
 */
public class SelectMarketActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + SelectMarketActivity.class.getSimpleName();

    // For the intent used in the MarketArrayAdapter Class
    public static final String EXTRA_MARKET = MainActivity.PROJECT_NAME + "EXTRA_MARKET";

    // Used to show the markets
    private List<Market> resultMarketList = new ArrayList<>();
    // Database with all favourite markets
    private MarketDataSource marketDataSource;

    private LayoutUtilsSelectMarket lu;

    /**
     * Called automatically when entering this the first time activity.
     * Sets the layout, the click listener for the search button, connects the database
     * and connects the adapter to the list view
     * @param savedInstanceState - save old state -
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Layout
        setContentView(LayoutUtilsSelectMarket.SELECT_MARKET_ACTIVITY);

        // Init Helper Class to handle the access to the layout elements
        lu = new LayoutUtilsSelectMarket(this);

        // Initialize the search button
        initMarketSearch();

        // Connect to favourite-market database
        marketDataSource = new MarketDataSource(this);

        // Initialize list view
        bindAdapterToListView();
    }

    /**
     * Called automatically every time entering this activity.
     * The database is opened and the favourite markets are shown.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "On Resume");

        // Open database connection
        marketDataSource.open();

        // Show Favourites
        showResults(marketDataSource.getAllFavouriteMarkets());
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
        marketDataSource.close();

    }

    /*************************************** OWN METHODS ****************************************/

    /**
     * Start the market search on button click in a new {@link RequestMarketsTask} instance.
     */
    private void initMarketSearch() {
        // Define On Click Button Reaction
        View.OnClickListener onSearchButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Search for Markets Button was clicked");

                // Get the requested city from the text view
                String requestedCity = lu.MARKET_SEARCH_FIELD_VIEW.getText().toString();

                // Start a new RequestMarketsTask to make the search
                new RequestMarketsTask().execute(requestedCity);
            }
        };

        // Add Reaction to Button
        lu.MARKET_SEARCH_BUTTON_VIEW.setOnClickListener(onSearchButtonClickListener);
    }

    /**
     * Set the {@link MarketArrayAdapter} to the list view element to display the markets correctly
     */
    private void bindAdapterToListView() {
        MarketArrayAdapter marketArrayAdapter =
                new MarketArrayAdapter(this, resultMarketList, marketDataSource, this);

        // Add adapter to list view
        lu.MARKET_RESULT_LIST_VIEW.setAdapter(marketArrayAdapter);
    }

    /**
     * Shows the results of the search, the number of found markets and the market list.
     * @param marketList list of markets to be displayed
     */
    protected void showResults(List<Market> marketList) {
        Log.v(LOG_TAG, "Updating view");

        // Update Header with number of found markets
        lu.MARKET_RESULT_HEADER_VIEW.setText("Gefundene Märkte: " + marketList.size());

        // Update result market list
        resultMarketList.clear();
        resultMarketList.addAll(marketList);

        // Update list View
        lu.MARKET_RESULT_LIST_VIEW.invalidateViews();
    }




    /*******************************************************************************************
     PRIVATE CLASS REQUEST-MARKETS-TASK
    1. Receives the requested city (String)
    2. Checks for markets with a server request.
    3. Returns a list of all found markets.
     ********************************************************************************************/
    private class RequestMarketsTask extends AsyncTask<String, String, List<Market>> {

        /**
         * Called when "execute" is called. Makes a server request with the city and returns
         * all found markets in a list.
         * @param requestedCity city which markets are returned
         * @return  list of found markets
         */
        @Override
        protected List<Market> doInBackground(String... requestedCity) {
            List<Market> marketList = new ArrayList<>();

            try {
                marketList = MarketUtils.requestMarketsFromServer(requestedCity[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException: " + e.getMessage());
                e.printStackTrace();
                publishProgress("Verbindung zum Server fehlgeschlagen. " +
                        "Es konnten keine Märkte gefunden werden.");
                return marketList;
            }

            if (marketList.isEmpty()) {
                publishProgress("Keine Märkte zu Ihrer Anfrage gefunden");
            }

            return marketList;
        }

        /**
         * Used by calling "publishProgress", posts update messages
         * @param stringParams  the message to pop up
         */
        @Override
        protected void onProgressUpdate(String... stringParams) {
            Toast.makeText(getApplicationContext(), stringParams[0], Toast.LENGTH_SHORT).show();
        }

        /**
         * When done return to main class to show the results.
         * (Called automatically when "doInBackground" has finished)
         * @param markets list with to found markets
         */
        @Override
        protected void onPostExecute(List<Market> markets) {
            showResults(markets);
        }
    }
}
