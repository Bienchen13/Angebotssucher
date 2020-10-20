package de.kathrin.angebote;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.kathrin.angebote.adapter.MarketArrayAdapter;
import de.kathrin.angebote.database.MarketDataSource;
import de.kathrin.angebote.models.Market;
import de.kathrin.angebote.utlis.LayoutUtilsSelectMarket;
import de.kathrin.angebote.utlis.MarketUtils;

import static de.kathrin.angebote.utlis.Strings.FOUND_MARKETS;
import static de.kathrin.angebote.utlis.Strings.NO_MARKETS_FOUND;
import static de.kathrin.angebote.utlis.Strings.NO_SERVER_CONNECTION;
import static de.kathrin.angebote.utlis.Strings.PROJECT_NAME;

/**
 * Activity to change the selected market
 */
public class SelectMarketActivity extends AppCompatActivity {

    private static final String LOG_TAG = PROJECT_NAME + SelectMarketActivity.class.getSimpleName();

    // Used to show the markets
    private final List<Market> resultMarketList = new ArrayList<>();
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

    // OWN METHODS

    /**
     * Start the market search on button or enter click in a new {@link RequestMarketsTask}
     * instance.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initMarketSearch() {

        // Start the search for markets, when enter is clicked
        lu.MARKET_SEARCH_FIELD_VIEW.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyEvent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    startSearch();
                    return true;
                }
                return false;
            }
        });

        // Start the search for markets, when the arrow is clicked
        final EditText editText = lu.MARKET_SEARCH_FIELD_VIEW;
        editText.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() + 50 >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                        startSearch();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Get the requested city from the text view and start a new {@link RequestMarketsTask} to
     * make the search
     */
    private void startSearch() {
        new RequestMarketsTask().execute(lu.MARKET_SEARCH_FIELD_VIEW.getText().toString());
    }

    /**
     * Set the {@link MarketArrayAdapter} to the list view element to display the markets correctly
     */
    private void bindAdapterToListView() {
        lu.MARKET_RESULT_LIST_VIEW.setAdapter(
                new MarketArrayAdapter(this, resultMarketList, marketDataSource, this)
        );
    }

    /**
     * Shows the results of the search, the number of found markets and the market list.
     * @param marketList list of markets to be displayed
     */
    protected void showResults(List<Market> marketList) {
        Log.v(LOG_TAG, "Updating view");

        // Update Header with number of found markets
        String header = FOUND_MARKETS + marketList.size();
        lu.MARKET_RESULT_HEADER_VIEW.setText(header);

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
    @SuppressLint("StaticFieldLeak")
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
                publishProgress(NO_SERVER_CONNECTION);
                return marketList;
            }

            if (marketList.isEmpty()) {
                publishProgress(NO_MARKETS_FOUND);
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
