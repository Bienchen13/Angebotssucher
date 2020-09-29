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

import java.util.ArrayList;
import java.util.List;

import de.kathrin.angebote.adapter.MarketArrayAdapter;
import de.kathrin.angebote.database.MarketDataSource;
import de.kathrin.angebote.models.Market;
import de.kathrin.angebote.utlis.MarketUtils;

/**
 * Activity to change the selected market
 */
public class SelectMarketActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + SelectMarketActivity.class.getSimpleName();
    // For the intent used in the MarketArrayAdapter Class
    public static final String EXTRA_MARKET = "de.kathrin.angebote.EXTRA_MARKET";

    // Used to show the markets
    private List<Market> resultMarketList = new ArrayList<>();
    // Database with all favourite markets
    private MarketDataSource marketDataSource;

    /**
     * Called automatically when entering this the first time activity.
     * Sets the layout, the click listener for the search button, connects the database
     * and connects the adapter to the list view
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Layout
        setContentView(R.layout.activity_select_market);

        // Define On Click Button Reaction
        View.OnClickListener onSearchButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Search for Markets Button was clicked");
                // Start the search for offers
                startMarketSearch();
            }
        };

        // Add Reaction to Button
        findViewById(R.id.market_search_button).setOnClickListener(onSearchButtonClickListener);

        // Connect to favourite market database
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
        marketDataSource.close();
        Log.v(LOG_TAG, "On Pause");
    }

    /*************************************** OWN METHODS ****************************************/

    /**
     * Set the {@link MarketArrayAdapter} to the list view element to display the markets correctly
     */
    private void bindAdapterToListView() {
        MarketArrayAdapter marketArrayAdapter =
                new MarketArrayAdapter(this, resultMarketList, marketDataSource, this);

        // Add adapter to list view
        ListView marketListView = findViewById(R.id.market_select_list);
        marketListView.setAdapter(marketArrayAdapter);
    }

    /**
     * Start the search for markets in the requested city.
     */
    private void startMarketSearch () {
        String requestedCity = ((EditText) findViewById(R.id.market_search_field)).getText().toString();

        // Start a new RequestMarketsTask to make the search
        RequestMarketsTask marketsTask = new RequestMarketsTask();
        marketsTask.execute(requestedCity);
    }

    /**
     * Shows the results of the search, the number of found markets and the market list.
     * @param marketList list of markets to be displayed
     */
    protected void showResults(List<Market> marketList) {
        Log.v(LOG_TAG, "Updating view");

        // Update Header with number of found markets
        TextView resultHeader = findViewById(R.id.market_select_header);
        resultHeader.setText("Gefundene Märkte: " + marketList.size());

        // Update result market list
        resultMarketList.clear();
        resultMarketList.addAll(marketList);

        // Update list View
        ListView listView = findViewById(R.id.market_select_list);
        listView.invalidateViews();
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
            List<Market> marketList = MarketUtils.requestMarketsFromServer(requestedCity[0]);

            if (marketList == null) {
                publishProgress("Verbindung zum Server fehlgeschlagen. " +
                        "Es konnten keine Märkte gefunden werden.");
            } else if (marketList.isEmpty()) {
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
