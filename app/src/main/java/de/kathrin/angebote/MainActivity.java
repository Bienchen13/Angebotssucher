package de.kathrin.angebote;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String PROJECT_NAME = "Angebote.";
    private static final String LOG_TAG = PROJECT_NAME + MainActivity.class.getSimpleName();

    static final int SELECT_MARKET_REQUEST = 1;
    static final int RESULT_OK = 0;

    private OfferList allOffersList = null;
    private List<Offer> resultOfferList = new ArrayList<>();
    private Market selectedMarket = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(LOG_TAG, "In Create");

        // Set Layout
        setContentView(R.layout.activity_main);

        // Set Default Market
        File defaultMarketFile = getFileStreamPath(Utility.DEFAULT_MARKET_FILE);
        if (defaultMarketFile.exists()) {
            selectedMarket = Utility.restoreMarketFromFile(this);
        } else {
            selectedMarket = new Market("2643422", "E center Köwe-Center",
                    "Dr.-Gessler-Straße 45", "Regensburg", "93051");
        }
        setSelectedMarket();

        // Define On Click Search Button Reaction
        View.OnClickListener onSearchButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Search for Offers Button was clicked");
                // Start the search for offers
                startOffersSearch();
              }
        };

        // Add On Click Reaction to Search Button
        findViewById(R.id.searchButton).setOnClickListener(onSearchButtonClickListener);

        // Define On Click Select Market Button Reaction
        View.OnClickListener onSelectMarketClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Select Market Button was clicked");
                // Start the search for offers
                startSelectMarketActivity();
            }
        };

        // Add On Click Reaction to Select Market Button
        findViewById(R.id.change_market).setOnClickListener(onSelectMarketClickListener);

        // Initialize Result List
        bindAdapterToListView();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "In onStop");
        Utility.saveMarketToFile(this, selectedMarket);
    }

    protected void startOffersSearch() {
        // Get search item from text field
        String searchItem = ((EditText) findViewById(R.id.searchField)).getText().toString();

        // Start the search
        RequestOffersTask offersTask = new RequestOffersTask();
        offersTask.execute(searchItem);
    }

    protected void startSelectMarketActivity() {
        Intent explicitIntent = new Intent(this, SelectMarketActivity.class);
        startActivityForResult(explicitIntent, SELECT_MARKET_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_MARKET_REQUEST) {
            if (resultCode == RESULT_OK) {
                selectedMarket = (Market) data.getSerializableExtra(SelectMarketActivity.EXTRA_MARKET);
                Log.v(LOG_TAG, "Received: " + selectedMarket.toString());

               setSelectedMarket();

                // Delete old offers
                allOffersList = null;
            } else {
                // Handle unsuccessful result
            }
        }
    }

    private void setSelectedMarket () {
        TextView selectMarketTextView = findViewById(R.id.select_market);
        selectMarketTextView.setText(
                selectedMarket.getName() + "\n" +
                selectedMarket.getStreet() + "\n" +
                selectedMarket.getPlz() + " "  + selectedMarket.getCity()
        );
    }

    private void bindAdapterToListView() {
        OfferArrayAdapter offerArrayAdapter = new OfferArrayAdapter(this, resultOfferList);
        ListView offerListView = findViewById(R.id.listview_activity_main);
        offerListView.setAdapter(offerArrayAdapter);
    }

    protected void updateListView(List<Offer> offerList) {
        Log.v(LOG_TAG, "Updating view");

        // Set Header with available dates
        TextView resultHeader = findViewById(R.id.resultHeader);
        resultHeader.setText("Angebote gültig vom " + allOffersList.getAvailableFromFormatted() +
                " bis zum " + allOffersList.getAvailableUntilFormatted() + ".");

        // Update result offer list
        resultOfferList.clear();
        resultOfferList.addAll(offerList);

        // Update list view
        ListView listView = findViewById(R.id.listview_activity_main);
        listView.invalidateViews();
    }


    /*
    PRIVATE CLASS REQUEST-OFFERS-TASK

    1. Receives the search parameter (String).
    2. Loads the current offers for the selected market from a file or
        makes a server request if necessary.
    3. Collects all offers which contain the search parameter.
    4. Returns a List of all requested offers.

     */

    private class RequestOffersTask extends AsyncTask<String, String, List<Offer>> {
        private String filename;

        @Override
        protected List<Offer> doInBackground(String... searchItem) {
            List<Offer> resultList = new ArrayList<>();
            filename = selectedMarket.getMarketID() + Utility.TEXTFILE_ENDING;

            // Request Offers from server, if necessary
            if (allOffersList == null) {

                // Load offers from file, if they exist
                File offerDataFile = getFileStreamPath(filename);

                if (offerDataFile.exists()) {
                    allOffersList = Utility.restoreOffersFromFile(MainActivity.this, filename);

                    if (allOffersList.getAvailableUntil().before(new Date())) {
                        Log.v(LOG_TAG, "Offers from file outdated");
                        allOffersList = null;

                        requestCurrentOffersFromServer();

                    } else {
                        Log.v(LOG_TAG, "Offers from file restored.");
                        //Log.v(LOG_TAG, allOffersList.toString());
                    }
                } else {
                    requestCurrentOffersFromServer();
                }
            }

            // collect all matching offers
            for (Offer o: allOffersList.getOfferList()) {
                if (o.getTitle().toLowerCase().contains(searchItem[0].toLowerCase()) ||
                    o.getDescription().toLowerCase().contains(searchItem[0].toLowerCase())) {
                    resultList.add(o);
                }
            }

            if (resultList.isEmpty()) {
                publishProgress("Keine Angebote zu Ihrer Anfrage gefunden");
            }

            return resultList;
        }

        @Override
        protected void onProgressUpdate(String... stringParams) {
            // post current status when publishProgress() in doInBackground() is called
            Toast.makeText(getApplicationContext(), stringParams[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(List<Offer> receivedOfferList) {
            // using result from doInBackground() function as parameter
            updateListView(receivedOfferList);
        }

        private void requestCurrentOffersFromServer () {
            String offers = Utility.requestOffersFromServer(selectedMarket);

            if (offers != null) {
                Log.v(LOG_TAG, offers);
                allOffersList = Utility.createOfferListFromJSONString(offers);

                Utility.saveOffersListInFile(MainActivity.this, allOffersList, filename);
                Log.v(LOG_TAG, "Stored offers in file");
            } else {
                Log.v(LOG_TAG, "Nothing received.");
                publishProgress("Verbindung zum Server fehlgeschlagen. " +
                        "Es konnten keine Angebote geladen werden.");
            }
        }
    }
}