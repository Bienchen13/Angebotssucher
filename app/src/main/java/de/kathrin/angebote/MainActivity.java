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

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
        registerListViewClickListener();
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

    private void registerListViewClickListener() {

        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.v(LOG_TAG, "Clicked on Item");

                // Get the information from offer object
                String titel = resultOfferList.get(position).getTitle();
                String description = resultOfferList.get(position).getDescription();
                String urlString = resultOfferList.get(position).getImageUrl();

                // Set up the popup
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.description_popup, null);

                // Get the elements from the popup view
                TextView titelView = popupView.findViewById(R.id.titel_popup_text);
                TextView descriptionView = popupView.findViewById(R.id.description_popup_text);
                ImageView imageView  = popupView.findViewById(R.id.popup_image);

                // Add the information to the corresponding view element
                titelView.setText(titel);
                descriptionView.setText(description);
                DownloadImageTask imageTask = new DownloadImageTask(imageView);
                imageTask.execute(urlString);

                // Show the Popup Window
                final PopupWindow pw = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, true);
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

        ListView listview = (ListView) findViewById(R.id.listview_activity_main);
        listview.setOnItemClickListener(onItemClickListener);
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


    /*
    PRIVATE CLASS DOWNLOAD-IMAGE-TASK

    1. Receives the image url (String).
    2. Loads the image from the internet.
    3. Resizes the image to the needed height and corresponding width.
    4. Sets the image into the given imageView.

     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {

            String urlString = urls[0];

            Bitmap bMap = null;

            try {
                URL url = new URL(urlString);
                InputStream in = url.openStream();
                bMap = BitmapFactory.decodeStream(in);

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

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                Log.v(LOG_TAG, "Result is null!");
            }
            imageView.setImageBitmap(result);
        }
    }


}