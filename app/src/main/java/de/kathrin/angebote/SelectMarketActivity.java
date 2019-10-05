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

public class SelectMarketActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + SelectMarketActivity.class.getSimpleName();
    private List<Market> resultMarketList = new ArrayList<>();
    public static final String EXTRA_MARKET = "de.kathrin.angebote.EXTRA_MARKET";
    private MarketDataSource marketDataSource;

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

        // Add On Click Reaction to Button
        findViewById(R.id.searchMarketButton).setOnClickListener(onSearchButtonClickListener);

        marketDataSource = new MarketDataSource(this);

        bindAdapterToListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        marketDataSource.open();
        Log.v(LOG_TAG, "On Resume");

        // Show Favourites
        List<Market> favouriteMarkets = marketDataSource.getAllFavouriteMarkets();
        //Log.v(LOG_TAG, "All favourite markets: " + favouriteMarkets);
        updateListView(favouriteMarkets);
    }

    @Override
    protected void onPause() {
        super.onPause();
        marketDataSource.close();
        Log.v(LOG_TAG, "On Pause");
    }

    private void bindAdapterToListView() {
        MarketArrayAdapter marketArrayAdapter =
                new MarketArrayAdapter(this, resultMarketList, marketDataSource, this);

        // Add adapter to list view
        ListView marketListView = findViewById(R.id.listview_select_market_activity);
        marketListView.setAdapter(marketArrayAdapter);
    }

    private void startMarketSearch () {
        String requestedCity = ((EditText) findViewById(R.id.searchMarketField)).getText().toString();

        RequestMarketsTask marketsTask = new RequestMarketsTask();
        marketsTask.execute(requestedCity);
    }

    protected void updateListView(List<Market> marketList) {
        Log.v(LOG_TAG, "Updating view");

        // Set Header with available markets
        TextView resultHeader = findViewById(R.id.select_market_header);
        resultHeader.setText("Gefundene Märkte: " + marketList.size());

        // Update result market list
        resultMarketList.clear();
        resultMarketList.addAll(marketList);

        // Update List View
        ListView listView = findViewById(R.id.listview_select_market_activity);
        listView.invalidateViews();
    }

     /*
    PRIVATE CLASS REQUEST-MARKETS-TASK

    1. Receives the requested city (String).
    2. Loads the current markets in the requested city with a server request.
    3. Returns a List of all found markets.

     */

    private class RequestMarketsTask extends AsyncTask<String, String, List<Market>> {

        @Override
        protected List<Market> doInBackground(String... requestedCity) {

            String marketString = Utility.requestMarketsFromServer(requestedCity[0]);
            List<Market> marketList = new ArrayList<>();

            if (marketString != null) {
                Log.v(LOG_TAG, marketString);
                marketList = Utility.createMarketListFromJSONString(marketString);
            } else {
                Log.v(LOG_TAG, "Nothing received.");
                publishProgress("Verbindung zum Server fehlgeschlagen. " +
                        "Es konnten keine Märkte gefunden werden.");
            }

            if (marketList.isEmpty()) {
                //resultList.add(new Offer("keine Eintraege gefunden", 0.0, ""));
                publishProgress("Keine Märkte zu Ihrer Anfrage gefunden");
            }

            return marketList;
        }

        @Override
        protected void onProgressUpdate(String... stringParams) {
            Toast.makeText(getApplicationContext(), stringParams[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(List<Market> markets) {
            updateListView (markets);
        }
    }
}
