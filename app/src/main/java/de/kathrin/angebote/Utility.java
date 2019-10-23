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

public class Utility {

    private static final String URL_EDEKA_OFFERS = "https://www.edeka.de/eh/service/eh/offers?";
    private static final String URL_EDEKA_MARKETS = "https://www.edeka.de/search.xml";
    private static final String LOG_TAG = MainActivity.PROJECT_NAME + Utility.class.getSimpleName();
    static final String TEXTFILE_ENDING = ".txt";
    static final String DEFAULT_MARKET_FILE = "default_market.txt";

    /*
     * Utility functions for handling Server Requests
     */

    private static String requestFromServer (String requestUrl, String requestMethod, String encodedData) {

        String resultString = null;
        HttpURLConnection conn = null;
        try {

            // Aufbauen der Verbindung zum Webserver - Timeout nach 9000ms
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(requestMethod);
            conn.setConnectTimeout(9000);
            conn.setReadTimeout(9000);

            if (encodedData != null) {
                Log.v(LOG_TAG, "POST Request");
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
                // Anfordern der Daten und Umwandeln dieser in eine Zeichenkette (String)
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

    /*
     * Utility functions for handling File Access
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

    /*
    * Utility functions for handling Offers
    */

    public static String requestOffersFromServer(Market market) {
        Log.v(LOG_TAG, "Request Offers from Server.");

        //String marketID = "192547";
        String marketID = market.getMarketID();
        String url = URL_EDEKA_OFFERS + "marketId=" + marketID + "&limit=89899";

        Log.v(LOG_TAG, "Url: " + url);

        return requestFromServer(url, "GET", null);
    }

    public static OfferList createOfferListFromJSONString(String jsonString) {

        OfferList offerList = new OfferList();
        List<Offer> receivedOffersList = new ArrayList<>();

        try {
            JSONObject jsonObj = new JSONObject(jsonString);

            long availableFrom = jsonObj.getLong("gueltig_von");
            long availableUntil = jsonObj.getLong("gueltig_bis");

            offerList.setAvailableFrom(new Date(availableFrom));
            offerList.setAvailableUntil(new Date(availableUntil));

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

                Offer newOffer = new Offer(title, price, description, imageUrl);

                receivedOffersList.add(newOffer);
                //Log.v(TAG, "Added: " + newOffer);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException: " + e.getMessage());
        }

        Log.v(LOG_TAG, "Added: " + receivedOffersList.size() + " Elements.");
        offerList.setOfferList(receivedOffersList);

        return offerList;
    }

    public static OfferList restoreOffersFromFile(Context context, String filename) {
        String jsonString = restoreStringFromFile(context, filename);
        return createOfferListFromJSONString(jsonString);
    }

    public static void saveOffersListInFile(Context context,OfferList offersList, String filename) {
        String jsonString = createJSONStringFromOffersList(offersList);
        saveStringInFile(context, jsonString, filename);
        Log.v(LOG_TAG, "Saved offers in file.");
    }

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

    /*
    * Utility functions for handling Markets
    */
    public static String requestMarketsFromServer(String city) {
        Log.v(LOG_TAG, "Request Markets from Server.");

        try {
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

        Log.v(LOG_TAG, "Data: " + data);

        return requestFromServer(URL_EDEKA_MARKETS, "POST", data);
    }

    public static List<Market> createMarketListFromJSONString (String jsonString) {

        List<Market> marketList = new ArrayList<>();

        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            JSONObject response = jsonObj.getJSONObject("response");
            JSONArray docs = response.getJSONArray("docs");

            // Run through docs-object, read offer data
            for (int i = 0; i < docs.length(); i++) {
                Market newMarket = getMarketFromJSONString(docs.getJSONObject(i));
                marketList.add(newMarket);
                Log.v(LOG_TAG, "Added: " + newMarket);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException: " + e.getMessage());
        }

        return marketList;
    }

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

    public static void saveMarketToFile (Context context, Market market) {
        String jsonString = createJSONStringFromMarket(market);
        saveStringInFile(context, jsonString, DEFAULT_MARKET_FILE);
        Log.v(LOG_TAG, "Market saved in File.");
    }

    public static Market restoreMarketFromFile (Context context) {
        String jsonString = restoreStringFromFile(context, DEFAULT_MARKET_FILE);
        Market m = null;

        try {
            m = getMarketFromJSONString(new JSONObject(jsonString));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException: " + e.getMessage());
        }

        return m;
    }
}
