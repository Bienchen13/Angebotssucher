package de.kathrin.angebote;

import android.content.Context;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class for handling server requests, file access and JSON conversions
 */
public class Utility {

    private static final String LOG_TAG = MainActivity.PROJECT_NAME + Utility.class.getSimpleName();

    // Server request URLs
    private static final String URL_EDEKA_OFFERS = "https://www.edeka.de/eh/service/eh/offers?";
    private static final String URL_EDEKA_MARKETS = "https://www.edeka.de/search.xml";

    // File Access Strings
    static final String TEXTFILE_ENDING = ".txt";
    static final String DEFAULT_MARKET_FILE = "default_market.txt";

    /*********************************************************************************************
     *
     *                               SERVER REQUESTS
     *
     * *******************************************************************************************/

    /**
     * Sends a request to the given URL and returns the response.
     * @param requestUrl    which URL is adressed
     * @param requestMethod for example POST or GET
     * @param encodedData   if its the search for a market: the requested city
     * @return  the server response
     */
    private static String requestFromServer (String requestUrl, String requestMethod, String encodedData) {

        String resultString = null;
        HttpURLConnection conn = null;
        try {

            // Establishing the connection to the web server - timeout after 9000ms
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(requestMethod);
            conn.setConnectTimeout(9000);
            conn.setReadTimeout(9000);

            // If not null, it is a market request. Otherwise an offer request.
            if (encodedData != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X " +
                        "10.14; rv:69.0) Gecko/20100101 Firefox/69.0");
                conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
                conn.setRequestProperty("Accept-Language", "en-GB,en;q=0.5");
                conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                conn.setRequestProperty("Referer", "https://www.edeka.de/marktsuche.jsp");

                OutputStream os = conn.getOutputStream();
                os.write(encodedData.getBytes());
            }

            Log.v(LOG_TAG, "ResponseCode: " + conn.getResponseCode());

            if (conn.getResponseCode() == 200) {
                // Request the data and convert it into a string
                InputStream stream = new BufferedInputStream(conn.getInputStream());
                resultString = convertStreamToString(stream);
            }

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(LOG_TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return resultString;
    }

    /**
     * Helper method for requestFromServer and restoreStringFromFile. Converts a InputStream into a string.
     * @param is    the InputStream
     * @return      the string
     */
    private static String convertStreamToString(InputStream is) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: " + e.getMessage());
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException: " + e.getMessage());
            }
        }

        return stringBuilder.toString();
    }


    /**********************************************************************************************
     *
     *                               FILE ACCESS
     *
     * *******************************************************************************************/

    /**
     * Write a string into a file.
     * @param context   current context
     * @param string    string that should be saved
     * @param filename  name of the file that is written
     */
    private static void saveStringInFile(Context context, String string, String filename) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fileOutputStream.write(string.getBytes());
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: " + e.getMessage());
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Write file content into a JSON string.
     * @param context   current context
     * @param filename  name of the file that it read
     * @return  JSON string with the file content
     */
    private static String restoreStringFromFile (Context context, String filename) {
        Log.v (LOG_TAG, "Restoring context!");
        String jsonString = "";

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = context.openFileInput(filename);
            InputStream stream = new BufferedInputStream(fileInputStream);

            jsonString = convertStreamToString(stream);
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "FileNotFoundException: " + e.getMessage());
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException: " + e.getMessage());
                }
            }
        }
        Log.v(LOG_TAG, "Restored: " + jsonString);
        return jsonString;
    }

    /**********************************************************************************************
     *
     *                               HANDLE OFFERS
     *
     * *******************************************************************************************/

    /**
     * Request all offers from one market from the server.
     * @param market    market that is used
     * @return          server response
     */
    public static String requestOffersFromServer(Market market) {
        Log.v(LOG_TAG, "Request Offers from Server.");

        // Compose URL with market ID
        String url = URL_EDEKA_OFFERS + "marketId=" + market.getMarketID() + "&limit=89899";

        return requestFromServer(url, "GET", null);
    }

    /**
     * Convert JSON String into an offerList instance.
     * @param jsonString string to convert
     * @return offerList instance
     */
    public static OfferList createOfferListFromJSONString(String jsonString) {

        OfferList offerList = new OfferList();
        List<Offer> receivedOffersList = new ArrayList<>();

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
                receivedOffersList.add(new Offer(title, price, description, imageUrl));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException: " + e.getMessage());
        }

        Log.v(LOG_TAG, "Added: " + receivedOffersList.size() + " Elements.");

        // Add the offer list to the offerList instance
        offerList.setOfferList(receivedOffersList);

        return offerList;
    }

    /**
     * Take a file and convert the content into an offerList instance.
     * (Combine restoreStringFromFile and createOfferListFromJSONString.)
     * @param context   current context
     * @param filename  file to convert
     * @return          offerList instance
     */
    public static OfferList restoreOffersFromFile(Context context, String filename) {
        return createOfferListFromJSONString(restoreStringFromFile(context, filename));
    }

    /**
     * Take an offerList instance and save it into a file.
     * (Combine createJSONStringFromOffersList and saveStringInFile.)
     * @param context       current context
     * @param offersList    offerList instance to be saved
     * @param filename      file where to save the offers
     */
    public static void saveOffersListInFile(Context context, OfferList offersList, String filename) {
        String jsonString = createJSONStringFromOffersList(offersList);
        saveStringInFile(context, jsonString, filename);
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

        for (Offer o: offerList.getOfferList()) {
            jsonString.append("{\"titel\":\"").append(o.getTitle())
                    .append("\",\"preis\":").append(o.getPrice())
                    .append(",\"beschreibung\":\"").append(o.getDescription())
                    .append("\",\"bild_app\":\"").append(o.getImageUrl())
                    .append("\"},");
        }

        jsonString.append("]");
        jsonString.append(", \"gueltig_von\":").append(offerList.getAvailableFromTime());
        jsonString.append(", \"gueltig_bis\":").append(offerList.getAvailableUntilTime());
        jsonString.append("}");

        return jsonString.toString();
    }


    /**********************************************************************************************
     *
     *                               HANDLE MARKETS
     *
     * *******************************************************************************************/

    /**
     * Request all markets in the given city from the server.
     * @param city  where to be searched
     * @return      JSON String with all markets in the city
     */
    public static String requestMarketsFromServer(String city) {
        Log.v(LOG_TAG, "Request Markets from Server.");

        try {
            // Handle umlauts
            city = URLEncoder.encode(city, "UTF-8");
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

        return requestFromServer(URL_EDEKA_MARKETS, "POST", data);
    }

    /**
     * Convert JSON String into list of markets
     * @param jsonString    JSON String to convert
     * @return  list of markets
     */
    public static List<Market> createMarketListFromJSONString (String jsonString) {

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

    /**
     * Write a market to a file (used to save the default market).
     * @param context   current context
     * @param market    default market
     */
    public static void saveMarketToFile (Context context, Market market) {
        String jsonString = createJSONStringFromMarket(market);
        saveStringInFile(context, jsonString, DEFAULT_MARKET_FILE);
    }

    /**
     * Read market from file (used to read the default market)
     * @param context   current context
     * @return          default market
     */
    public static Market restoreMarketFromFile (Context context) {
        try {

            String jsonString = restoreStringFromFile(context, DEFAULT_MARKET_FILE);
            return getMarketFromJSONString(new JSONObject(jsonString));

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException: " + e.getMessage());
            return null;
        }
    }
}
