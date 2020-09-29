package de.kathrin.angebote;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.kathrin.angebote.adapter.OfferArrayAdapter;
import de.kathrin.angebote.models.Market;
import de.kathrin.angebote.models.Offer;
import de.kathrin.angebote.models.OfferList;
import de.kathrin.angebote.utlis.MarketUtils;
import de.kathrin.angebote.utlis.OfferUtils;

/**
 * Main Activity. Search for offers or go to the Select Market Activity.
 */
public class MainActivity extends AppCompatActivity {

    public static final String PROJECT_NAME = "Angebote.";
    private static final String LOG_TAG = PROJECT_NAME + MainActivity.class.getSimpleName();

    public static final int SELECT_MARKET_REQUEST = 1;
    public static final int RESULT_OK = 0;

    private OfferList allOffersList = null;
    private List<Offer> resultOfferList = new ArrayList<>();
    private Market selectedMarket = null;


    /**
     * Automatically called when starting the app.
     * Sets the Layout, the default market (if it exists), initializes the search button,
     * the select-market button and the result list.
     * @param savedInstanceState to reconstruct the old state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "In Create");

        // Set Layout
        setContentView(R.layout.activity_main);

        // Set Default Market
        setDefaultMarket();

        // Initialize the Search button
        initOfferSearch();

        // Initialize the Select Market button
        initMarketSelection();

        // Initialize Result List
        initListView();
    }

    /**
     * Read last used market from file and set it as default market.
     */
    private void setDefaultMarket () {
        selectedMarket = MarketUtils.restoreMarketFromFile(this);

        if (selectedMarket != null) {
            setSelectedMarket();
        }
    }

    /**
     * Display the selected market.
     */
    private void setSelectedMarket () {
        String market = selectedMarket.getName(); //+ "\n" +
                        //selectedMarket.getStreet() + "\n" +
                        // selectedMarket.getPlz() + " "  +
                        //selectedMarket.getCity();

        ((TextView) findViewById(R.id.select_market)).setText(market);
    }

    /**
     * Start the offer search on button click in a new {@link RequestOffersTask} instance.
     */
    private void initOfferSearch() {

        // Start the search for offers, when the button was clicked.
        View.OnClickListener onSearchButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Search for Offers Button was clicked");

                // Get search item from text field
                String searchItem = ((EditText) findViewById(R.id.offer_search_field)).getText().toString();

                // Start the search
                RequestOffersTask offersTask = new RequestOffersTask();
                offersTask.execute(searchItem);
            }
        };

        // Add On Click Reaction to Search Button
        findViewById(R.id.offer_search_button).setOnClickListener(onSearchButtonClickListener);
    }

    /**
     * Switch to the Select Market Activity on button click.
     */
    private void initMarketSelection() {
        // Switch to the Select Market Activity, when button was clicked.
        View.OnClickListener onSelectMarketClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Select Market Button was clicked");

                Intent explicitIntent = new Intent(MainActivity.this, SelectMarketActivity.class);
                startActivityForResult(explicitIntent, SELECT_MARKET_REQUEST);
            }
        };

        // Add On Click Reaction to Select Market Button
        findViewById(R.id.select_market).setOnClickListener(onSelectMarketClickListener);

    }

    /**
     * Bind the Adapter to the List View and init the click reaction on the offers.
     */
    private void initListView() {

        // Bind Adapter to List View
        OfferArrayAdapter offerArrayAdapter = new OfferArrayAdapter(this, resultOfferList);
        ((ListView)findViewById(R.id.offer_result_list)).setAdapter(offerArrayAdapter);

        // Add pop-up with description when clicking on an offer.
        registerListViewClickListener();
    }

    /**
     * Automatically called when leaving the activity.
     * Saves the current market in a file.
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "In onStop");

        if (selectedMarket != null) {
            MarketUtils.saveMarketToFile(this, selectedMarket);
        }
    }

    /**
     * Automatically called when returning from the Select Market Activity.
     * Sets the returned market and deletes old offers.
     * @param requestCode - done by Java
     * @param resultCode - done by Java
     * @param data - done by Java
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_MARKET_REQUEST) {
            if (resultCode == RESULT_OK) {

                if (data != null) {
                    selectedMarket = (Market) data.getSerializableExtra(SelectMarketActivity.EXTRA_MARKET);
                    setSelectedMarket();
                }

                // Delete old offers
                allOffersList = null;

                // Clear list view
                resultOfferList.clear();
                ((ListView)findViewById(R.id.offer_result_list)).invalidateViews();
                ((TextView)findViewById(R.id.offer_result_header)).setText("");
            }
        }
    }

    /**
     * Open a new pop-up window with further information (description, image) when clicking
     * on an offer.
     */
    private void registerListViewClickListener() {

        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the information from offer object
                String title = resultOfferList.get(position).getTitle();
                String description = resultOfferList.get(position).getDescription();
                String urlString = resultOfferList.get(position).getImageUrl();

                // Set up the popup
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.description_popup, null);

                // Connect the elements from the popup view with the corresponding information
                ((TextView) popupView.findViewById(R.id.popup_title)).setText(title);
                ((TextView) popupView.findViewById(R.id.popup_description)).setText(description);

                // Start a new DownloadImageTask to display the image
                ImageView imageView  = popupView.findViewById(R.id.popup_image);
                new DownloadImageTask(imageView).execute(urlString);

                // Show the Popup Window
                final PopupWindow pw = new PopupWindow(popupView,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        true);
                pw.showAtLocation(view, Gravity.CENTER, 0, 0);

                // Add on Touch Listener to close popup window easily
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        pw.dismiss();
                        return true;
                    }
                });
            }
        };

        ((ListView) findViewById(R.id.offer_result_list)).setOnItemClickListener(onItemClickListener);
    }

    /**
     * Update result header and offer list.
     * @param offerList new offers to show in the result
     */
    protected void updateListView(List<Offer> offerList) {
        Log.v(LOG_TAG, "Updating view");

        // Set Header with available dates
        String headerText = "Gültig: " +
                allOffersList.getAvailableFromFormatted() +
                " - " +
                allOffersList.getAvailableUntilFormatted();
        ((TextView)findViewById(R.id.offer_result_header)).setText(headerText);

        // Update result offer list
        resultOfferList.clear();
        resultOfferList.addAll(offerList);

        // Update list view
        ((ListView)findViewById(R.id.offer_result_list)).invalidateViews();
    }

    /**
     *  PRIVATE CLASS REQUEST-OFFERS-TASK
     *
     *     1. Receives the search parameter (String).
     *     2. Loads the current offers for the selected market from a file or
     *         makes a server request if necessary.
     *     3. Collects all offers which contain the search parameter.
     *     4. Returns a list of all requested offers.
     *
     */
    private class RequestOffersTask extends AsyncTask<String, String, List<Offer>> {

        /**
         * First load all offers of the selected market. (If there are not there, look
         * in the file or make a server request.)
         * Then search in the offers for offers matching the request string. Return those.
         * @param searchItem request string
         * @return list of offers matching the request string
         */
        @Override
        protected List<Offer> doInBackground(String... searchItem) {
            List<Offer> resultList = new ArrayList<>();

            // May happen when starting the app the first time.
            if (selectedMarket == null) {
                publishProgress("Wählen Sie zu erst einen Markt aus!");
                return resultList;
            }

            // Try to restore offers from file
            if (allOffersList == null) {
                allOffersList = OfferUtils.restoreOffersFromFile(MainActivity.this, selectedMarket);

                // Check if the file is outdated
                if (allOffersList != null && allOffersList.getAvailableUntil().before(new Date())) {
                    Log.v(LOG_TAG, "Offers from file outdated");
                    allOffersList = null;
                }
            }

            // Try to make a server request to load offers
            if (allOffersList == null) {
                allOffersList = OfferUtils.requestOffersFromServer(MainActivity.this, selectedMarket);
            }

            // if list is still null, something bad happened
            if (allOffersList == null) {
                publishProgress("Verbindung zum Server fehlgeschlagen. " +
                        "Es konnten keine Angebote geladen werden.");
                return resultList;
            }

            String requestString = searchItem[0].toLowerCase().trim();

            // collect all matching offers
            for (Offer o: allOffersList) {
                if (o.getTitle().toLowerCase().contains(requestString) ||
                    o.getDescription().toLowerCase().contains(requestString)) {
                    resultList.add(o);
                }
            }

            if (resultList.isEmpty()) {
                publishProgress("Keine Angebote zu Ihrer Anfrage gefunden");
            }

            return resultList;
        }

        /**
         * Inform about the ongoing process.
         * @param stringParams message to display
         */
        @Override
        protected void onProgressUpdate(String... stringParams) {
            // posts current status when publishProgress() in doInBackground() is called
            Toast.makeText(getApplicationContext(), stringParams[0], Toast.LENGTH_SHORT).show();
        }

        /**
         * Return the matching offers to the main activity, if there are any.
         * @param receivedOfferList matching offers found
         */
        @Override
        protected void onPostExecute(List<Offer> receivedOfferList) {
            // using result from doInBackground() function as parameter
            if (allOffersList != null) {
                updateListView(receivedOfferList);
            }
        }
    }
    
    /**
     * PRIVATE CLASS DOWNLOAD-IMAGE-TASK
     *
     * 1. Receives the image url (String).
     * 2. Loads the image from the internet.
     * 3. Resizes the image to the needed height and corresponding width.
     * 4. Sets the image into the given imageView.
     *
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        /**
         * Constructor, saving the needed image view.
         * @param imageView place where the image is shown in the end
         */
        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        /**
         * Loads the image and resizes it.
         * @param urls image url
         * @return the image bitmap
         */
        @Override
        protected Bitmap doInBackground(String... urls) {

            String urlString = urls[0];

            Bitmap bMap = null;

            try {
                // Load image
                URL url = new URL(urlString);
                InputStream in = url.openStream();
                bMap = BitmapFactory.decodeStream(in);

                // Resize to the correct format
                int newImageHeight = 600;
                int newImageWidth = bMap.getWidth() * newImageHeight / bMap.getHeight();
                bMap = Bitmap.createScaledBitmap(bMap, newImageWidth, newImageHeight, true);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bMap;
        }

        /**
         * Connects the image bitmap with the imageView.
         * @param result image bitmap
         */
        @Override
        protected void onPostExecute(Bitmap result) {
            // Add the image to the imageView
            imageView.setImageBitmap(result);
        }
    }


}