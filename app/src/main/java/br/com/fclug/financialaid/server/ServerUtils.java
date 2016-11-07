package br.com.fclug.financialaid.server;

import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Fabioclug on 2016-09-05.
 */
public class ServerUtils {
    private static final String TAG = "ServerUtils";
    private static String EMULATOR_SERVER_URL = "http://10.0.2.2:8080/api";
    private static String SERVER_URL = "http://192.168.1.40:8080/api";

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    public static final String ROUTE_TEST = "/";
    public static final String ROUTE_CHECK_USERNAME = "/checkUsername";
    public static final String ROUTE_LOGIN = "/login";
    public static final String ROUTE_REGISTER_USER = "/register";
    public static final String ROUTE_CREATE_GROUP = "/createGroup";
    public static final String ROUTE_CREATE_GROUP_TRANSACTION = "/createGroupTransaction";
    public static final String ROUTE_GET_GROUPS = "/getGroups";
    public static final String ROUTE_GET_USERS = "/getUsers";

    public static final int TIMEOUT = 15000;

    private static JSONObject readResponse(BufferedReader reader) throws JSONException {
        String line;
        StringBuilder builder = new StringBuilder();

        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
        } catch (Exception e){
            Log.d(TAG, "Can't read response:" + e.toString());
        }
        String responseString = builder.toString();
        JSONObject response = null;
        if (!responseString.isEmpty()) {
            response = new JSONObject(responseString);
        }
        return response;
    }

    public static JSONObject doGetRequest(String route) throws IOException, JSONException {
        String requestURL = SERVER_URL + route;
        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.connect();

        // get the server response
        BufferedReader response = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return readResponse(response);
    }

    public static JSONObject doPostRequest(String route, JSONObject args) throws IOException, JSONException {

        if (isEmulator()) {
            SERVER_URL = EMULATOR_SERVER_URL;
        }
        // set up the connection
        String requestURL = SERVER_URL + route;
        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(METHOD_POST);
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setDoOutput(true);
        //connection.setChunkedStreamingMode(0);
        connection.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.addRequestProperty("Accept", "application/json");

        // write the json object to the request and post
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(args.toString());
        writer.flush();

        // get the server response
        BufferedReader response = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return readResponse(response);
    }

    public static boolean isApiOn() {
        ApiRequest request = new ApiRequest(METHOD_GET, ROUTE_TEST, null, null);
        request.execute();
        JSONObject response = null;
        try {
            response = request.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException |TimeoutException e) {
            e.printStackTrace();
        }

        boolean result = false;
        try {
            int code = response != null ? response.getInt("status_code") : 0;
            if (code == HttpURLConnection.HTTP_OK) {
                result = true;
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSON problem: " + e.toString());
        }
        return result;
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
}
