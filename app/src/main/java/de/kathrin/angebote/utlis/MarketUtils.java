package de.kathrin.angebote.utlis;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import de.kathrin.angebote.models.Market;

import static de.kathrin.angebote.utlis.Strings.DEFAULT_MARKET_FILE;
import static de.kathrin.angebote.utlis.Strings.PROJECT_NAME;
import static de.kathrin.angebote.utlis.Strings.URL_EDEKA_MARKETS;
import static de.kathrin.angebote.utlis.Strings.UTF8;

public class MarketUtils {

    private static final String LOG_TAG = PROJECT_NAME + MarketUtils.class.getSimpleName();

    // PUBLIC FUNCTIONS

    /**
     * Request all markets in the given city from the server.
     * @param city  where to be searched
     * @return      List with all markets in the city
     */
    public static List<Market> requestMarketsFromServer(String city) throws IOException {
        Log.v(LOG_TAG, "Request Markets from Server.");

        try {
            // Handle umlauts
            city = URLEncoder.encode(city, UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String data = "indent=off" +
                "&hl=false" +
                "&rows=400" +
                "&q=((indexName%3Ab2cMarktDBIndex++AND+kanalKuerzel_tlcm%3Aedeka+AND+" +
                "((freigabeVonDatum_longField_l%3A%5B0+TO+1569362399999%5D+AND+" +
                "freigabeBisDatum_longField_l%3A%5B1569276000000+TO+*%5D)+AND+NOT+" +
                "(datumAppHiddenVon_longField_l%3A%5B0+TO+1569362399999%5D+AND+" +
                "datumAppHiddenBis_longField_l%3A%5B1569276000000+TO+*%5D))+AND+" +
                "ort_tlc%3A" + city.toLowerCase() + "" +
                "))&fl=marktID_tlc%2Cplz_tlc%2Cort_tlc%2Cstrasse_tlc%2Cname_tlc";

        // Request URL
        String jsonString = IOUtils.requestFromServer(URL_EDEKA_MARKETS, "POST", data);
        List<Market> marketList = null;

        if (jsonString != null) {
            // Convert JSON to market list
            marketList = createMarketListFromJSONString(jsonString);
        } else {
            Log.v(LOG_TAG, "Nothing received.");
        }

        return marketList;
    }

    /**
     * Write a market to a file (used to save the default market).
     * @param context   current context
     * @param market    default market
     */
    public static void saveMarketToFile (Context context, Market market) {
        String jsonString = createJSONStringFromMarket(market);
        IOUtils.saveStringInFile(context, jsonString, DEFAULT_MARKET_FILE);
    }

    /**
     * Read market from file (used to read the default market)
     * @param context   current context
     * @return          default market
     */
    public static Market restoreMarketFromFile (Context context) {
        try {
            // Check if file exists
            if (context.getFileStreamPath(DEFAULT_MARKET_FILE).exists()) {
                // Restore JSON from File
                String jsonString = IOUtils.restoreStringFromFile(context, DEFAULT_MARKET_FILE);
                // Convert JSON to Market
                return getMarketFromJSONString(new JSONObject(jsonString));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException: " + e.getMessage());
        }
        return null;
    }

    // PRIVATE HELPER FUNCTIONS

    /**
     * Convert JSON String into list of markets
     * @param jsonString    JSON String to convert
     * @return  list of markets
     */
    private static List<Market> createMarketListFromJSONString (String jsonString) {

        List<Market> marketList = new ArrayList<>();

        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            JSONArray docs = jsonObj.getJSONObject("response").getJSONArray("docs");

            // Run through docs-object, read offer data
            for (int i = 0; i < docs.length(); i++) {
                Market newMarket = getMarketFromJSONString(docs.getJSONObject(i));
                marketList.add(newMarket);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException: " + e.getMessage());
        }

        return marketList;
    }

    /**
     * Helper function to convert a JSON Object into a single market instance.
     * (Used in createMarketListFromJSONString and restoreMarketFromFile)
     * @param jsonObject    JSON Object to convert
     * @return              market instance
     */
    private static Market getMarketFromJSONString (JSONObject jsonObject) {
        try {
            String id = jsonObject.getString("marktID_tlc");
            String street = jsonObject.getString("strasse_tlc");
            String city = jsonObject.getString("ort_tlc");
            String name = jsonObject.getString("name_tlc");
            String plz = jsonObject.getString("plz_tlc");

            return new Market(id, name, street, city, plz);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException: " + e.getMessage());
        }
        return null;
    }

    /**
     * Helper function to convert a market instance into a JSON string.
     * (Used in saveMarketToFile)
     * @param market    market to be converted
     * @return  JSON string
     */
    private static String createJSONStringFromMarket (Market market) {
        String jsonString = "";

        jsonString += "{";
        jsonString += "\"marktID_tlc\":\"" + market.getMarketID() + "\", ";
        jsonString += "\"name_tlc\":\"" + market.getName() + "\", ";
        jsonString += "\"strasse_tlc\":\"" + market.getStreet() + "\", ";
        jsonString += "\"ort_tlc\":\"" + market.getCity() + "\", ";
        jsonString += "\"plz_tlc\":\"" + market.getPlz() + "\"";
        jsonString += "}";

        return jsonString;
    }

}
