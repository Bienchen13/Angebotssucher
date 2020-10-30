package de.kathrin.angebote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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
import de.kathrin.angebote.utlis.LayoutUtilsMain;
import de.kathrin.angebote.utlis.MarketUtils;
import de.kathrin.angebote.utlis.OfferUtils;

import static de.kathrin.angebote.utlis.Strings.EXTRA_MARKET;
import static de.kathrin.angebote.utlis.Strings.NO_MARKET_SELECTED;
import static de.kathrin.angebote.utlis.Strings.NO_OFFERS_FOUND;
import static de.kathrin.angebote.utlis.Strings.NO_SERVER_CONNECTION;
import static de.kathrin.angebote.utlis.Strings.PROJECT_NAME;

/**
 * Main Activity. Search for offers or go to the Select Market Activity.
 */
public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = PROJECT_NAME + MainActivity.class.getSimpleName();

    public static final int SELECT_MARKET_REQUEST = 1;
    public static final int RESULT_OK = 0;

    private OfferList allOffersList = null;
    private final List<Offer> resultOfferList = new ArrayList<>();
    private Market selectedMarket = null;

    private LayoutUtilsMain lu;

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
        setContentView(LayoutUtilsMain.MAIN_ACTIVITY);

        // Init Helper Class to handle the access to the layout elements
        lu = new LayoutUtilsMain(this);

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

        lu.MARKET_SELECT_VIEW.setText(market);
    }

    /**
     * Start the offer search on button or enter click in a new {@link RequestOffersTask} instance.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initOfferSearch() {

        // Start the search for offers, when enter is clicked
        lu.OFFER_SEARCH_FIELD_VIEW.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyEvent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    startSearch();
                    return true;
                }
                return false;
            }
        });

        // Start the search for offers, when the arrow is clicked
        final EditText editText = lu.OFFER_SEARCH_FIELD_VIEW;
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
     * Get search item from text field and start the search in a new {@link RequestOffersTask}
     * instance.
     */
    private void startSearch() {
        RequestOffersTask offersTask = new RequestOffersTask();
        offersTask.execute(lu.OFFER_SEARCH_FIELD_VIEW.getText().toString());
    }

    /**
     * Switch to the Select Market Activity on button click.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initMarketSelection() {

        // Add action on click on the market name.
        final TextView textView = lu.MARKET_SELECT_VIEW;
        textView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    // Switch to the Select Market Activity, when the pencil is clicked.
                    if (event.getRawX() + 50 >= (textView.getRight() - textView.getCompoundDrawables()[2].getBounds().width())) {
                        Log.v(LOG_TAG, "Select Market Pencil was clicked");

                        Intent explicitIntent = new Intent(MainActivity.this, SelectMarketActivity.class);
                        startActivityForResult(explicitIntent, SELECT_MARKET_REQUEST);
                    } else {

                        // Otherwise show all information of the selected market
                        Log.v(LOG_TAG, "Market Name was clicked");

                        if (selectedMarket != null) {
                            showWholeMarketName(textView);
                        } else {
                            Toast.makeText(getApplicationContext(), NO_MARKET_SELECTED, Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Create pop up with the whole market information.
     * @param view  clicked view
     */
    private void showWholeMarketName(View view) {
        // Set up the popup
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(LayoutUtilsMain.MARKET_POPUP, null);
        lu.setMarketPopupView(popupView);

        // Connect the elements from the popup view with the corresponding information
        String market = selectedMarket.getName()+ "\n" +
                selectedMarket.getStreet() + "\n" +
                selectedMarket.getPlz() + " "  +
                selectedMarket.getCity();

        lu.MARKET_POPUP_CONTENT_VIEW.setText(market);

        // Show the Popup Window
        final PopupWindow pw = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);
        pw.showAtLocation(view, Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, view.getBottom());

        // Add on Touch Listener to close popup window easily
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pw.dismiss();
                return true;
            }
        });
    }

    /**
     * Bind the Adapter to the List View and init the click reaction on the offers.
     */
    private void initListView() {

        // Bind Adapter to List View
        OfferArrayAdapter offerArrayAdapter = new OfferArrayAdapter(this, resultOfferList);
        lu.OFFER_RESULT_LIST_VIEW.setAdapter(offerArrayAdapter);

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
     * Create an options menu to set the notifications
     * @param menu  - done by Java -
     * @return      true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set_notification, menu);
        return true;
    }

    /**
     * Called, when an element in the options menu is clicked
     * @param item  clicked item
     * @return      boolean, true if everything is fine
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_show_notifications) {
            Log.v(LOG_TAG, "Clicked on set notifications!");

            // Switch to Notification Activity
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);

            return true;
        } else {
            return super.onOptionsItemSelected(item);
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
                    selectedMarket = (Market) data.getSerializableExtra(EXTRA_MARKET);
                    setSelectedMarket();
                }

                // Delete old offers
                allOffersList = null;

                // Clear list view
                resultOfferList.clear();
                lu.OFFER_RESULT_LIST_VIEW.invalidateViews();
                lu.OFFER_RESULT_HEADER_VIEW.setText("");
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
                View popupView = inflater.inflate(LayoutUtilsMain.OFFER_POPUP, null);
                lu.setPopupView(popupView);

                // Connect the elements from the popup view with the corresponding information
                lu.OFFER_POPUP_TITLE_VIEW.setText(title);
                lu.OFFER_POPUP_DESCRIPTION_VIEW.setText(description);

                // Start a new DownloadImageTask to display the image
                new DownloadImageTask(lu.OFFER_POPUP_IMAGE_VIEW).execute(urlString);

                // Show the Popup Window
                final PopupWindow pw = new PopupWindow(popupView,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        true);
                pw.showAtLocation(view, Gravity.CENTER, 0, 0);

                // Add on Touch Listener to close popup window easily
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        pw.dismiss();
                        return true;
                    }
                });
            }
        };

        lu.OFFER_RESULT_LIST_VIEW.setOnItemClickListener(onItemClickListener);
    }

    /**
     * Update result header and offer list.
     * @param offerList new offers to show in the result
     */
    protected void updateListView(List<Offer> offerList) {
        Log.v(LOG_TAG, "Updating view");

        // Set Header with available dates
        String headerText =
                //"Gültig: " +
                allOffersList.getAvailableFromFormatted() +
                " - " +
                allOffersList.getAvailableUntilFormatted();
        lu.OFFER_RESULT_HEADER_VIEW.setText(headerText);

        // Update result offer list
        resultOfferList.clear();
        resultOfferList.addAll(offerList);

        // Update list view
        lu.OFFER_RESULT_LIST_VIEW.invalidateViews();
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
    @SuppressLint("StaticFieldLeak")
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
                publishProgress(NO_MARKET_SELECTED);
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

                try {
                    allOffersList = OfferUtils.requestOffersFromServer(MainActivity.this, selectedMarket);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException: " + e.getMessage());
                    publishProgress(NO_SERVER_CONNECTION);
                    return resultList;
                }
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
                publishProgress(NO_OFFERS_FOUND);
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
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @SuppressLint("StaticFieldLeak")
        final ImageView imageView;

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