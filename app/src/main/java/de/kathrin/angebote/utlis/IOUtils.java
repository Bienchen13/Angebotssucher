package de.kathrin.angebote.utlis;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static de.kathrin.angebote.utlis.Strings.PROJECT_NAME;

/**
 * Utility class for handling server requests and file access
 */
public class IOUtils {

    private static final String LOG_TAG = PROJECT_NAME + IOUtils.class.getSimpleName();

    //                  SERVER REQUESTS

    /**
     * Sends a request to the given URL and returns the response.
     * @param requestUrl    which URL is addressed
     * @param requestMethod for example POST or GET
     * @param encodedData   if its the search for a market: the requested city
     * @return  the server response
     */
    static String requestFromServer (String requestUrl, String requestMethod, String encodedData) throws IOException{

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
    static String convertStreamToString(InputStream is) {
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


    //                      FILE ACCESS

    /**
     * Write a string into a file.
     * @param context   current context
     * @param string    string that should be saved
     * @param filename  name of the file that is written
     */
    static void saveStringInFile(Context context, String string, String filename) {
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
    static String restoreStringFromFile (Context context, String filename) {
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
}
