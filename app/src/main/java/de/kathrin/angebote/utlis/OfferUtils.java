package de.kathrin.angebote.utlis;

import android.content.Context;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import de.kathrin.angebote.MainActivity;
import de.kathrin.angebote.models.Market;
import de.kathrin.angebote.models.Offer;
import de.kathrin.angebote.models.OfferList;

public class OfferUtils {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + OfferUtils.class.getSimpleName();

    //      PUBLIC FUNCTIONS

    /**
     * Request all offers from one market from the server.
     * @param market    market that is used
     * @return          server response
     */
    public static OfferList requestOffersFromServer(Context context, Market market) throws IOException {
        Log.v(LOG_TAG, "Request Offers from Server.");

        // Compose URL with market ID
        String url = IOUtils.URL_EDEKA_OFFERS + "marketId=" + market.getMarketID() + "&limit=89899";

        // Request URL
        String jsonString = IOUtils.requestFromServer(url, "GET", null);

        OfferList offerList = null;

        if (jsonString != null) {
            offerList = createOfferListFromJSONString(jsonString);
            saveOffersListInFile(context, offerList, getOfferFilename(market));
            Log.v(LOG_TAG, "Stored offers in file");
        } else {
            Log.v(LOG_TAG, "Nothing received.");
        }
        return offerList;
    }

    /**
     * Take a file (found by the market id) and convert the content into an offerList instance.
     * (Combine restoreStringFromFile and createOfferListFromJSONString.)
     * @param context   current context
     * @param market    market of which the offers are restored
     * @return          offerList instance
     */
    public static OfferList restoreOffersFromFile(Context context, Market market) {

        String filename = getOfferFilename(market);

        if (context.getFileStreamPath(filename).exists()) {
            return createOfferListFromJSONString(IOUtils.restoreStringFromFile(context, filename));
        }
        return null;
    }


    // PRIVATE HELPER FUNCTIONS

    /**
     * Convert JSON String into an offerList instance.
     * @param jsonString string to convert
     * @return offerList instance
     */
    private static OfferList createOfferListFromJSONString(String jsonString) {
        OfferList offerList = new OfferList();

        try {
            JSONObject jsonObj = new JSONObject(jsonString);

            // Get period of validity
            offerList.setAvailableFrom(new Date(jsonObj.getLong("gueltig_von")));
            offerList.setAvailableUntil(new Date(jsonObj.getLong("gueltig_bis")));

            // Demand JSON Array with Offer-Objects
            JSONArray docs = jsonObj.getJSONArray("docs");

            // Run through docs-object, read offer data
            for (int i = 0; i < docs.length(); i++) {
                JSONObject offer = docs.getJSONObject(i);

                String title = offer.getString("titel")
                        .replace("\n", " ")
                        .trim();
                Double price = offer.getDouble("preis");
                String description = Html.fromHtml(offer.getString("beschreibung"))
                        .toString()
                        .replace("\n", " ")
                        .trim();
                String imageUrl = offer.getString("bild_app");

                // Create new offer instance and add it to the list
                offerList.add(new Offer(title, price, description, imageUrl));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException: " + e.getMessage());
        }

        Log.v(LOG_TAG, "Added: " + offerList.size() + " Elements.");
        //Log.v(LOG_TAG, offerList.toString());

        return offerList;
    }

    /**
     * Helper function for saveOffersListInFile.
     * Convert offerList instance into a JSON string.
     * Has to have the same format as the server response!
     *
     * @param offerList offerList to convert
     * @return  JSON string
     */
    private static String createJSONStringFromOffersList (OfferList offerList) {
        StringBuilder jsonString = new StringBuilder();

        jsonString.append("{\"docs\":[");

        for (Offer o: offerList) {  // Escape possible quotes from title and description, it crashes
            jsonString.append("{\"titel\":\"").append(o.getTitle().replace("\"", "\\\""))
                    .append("\",\"preis\":").append(o.getPrice())
                    .append(",\"beschreibung\":\"").append(o.getDescription().replace("\"", "\\\""))
                    .append("\",\"bild_app\":\"").append(o.getImageUrl())
                    .append("\"},");
        }

        jsonString.append("]");
        jsonString.append(", \"gueltig_von\":").append(offerList.getAvailableFromTime());
        jsonString.append(", \"gueltig_bis\":").append(offerList.getAvailableUntilTime());
        jsonString.append("}");

        return jsonString.toString();
    }

    /**
     * Take an offerList instance and save it into a file.
     * (Combine createJSONStringFromOffersList and saveStringInFile.)
     * @param context       current context
     * @param offersList    offerList instance to be saved
     * @param filename      file where to save the offers
     */
    private static void saveOffersListInFile(Context context, OfferList offersList, String filename) {
        String jsonString = createJSONStringFromOffersList(offersList);
        IOUtils.saveStringInFile(context, jsonString, filename);
    }

    /**
     * Get the filename, where all offers of a market are saved.
     * @param m     market which offers are searched
     * @return      filename
     */
    private static String getOfferFilename (Market m) {
        return m.getMarketID() + IOUtils.TEXTFILE_ENDING;
    }

}
