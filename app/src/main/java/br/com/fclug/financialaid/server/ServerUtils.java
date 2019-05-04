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
import java.net.SocketTimeoutException;
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
    private static String SERVER_URL = "http://ec2-18-217-92-219.us-east-2.compute.amazonaws.com:8080";

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    public static final String ROUTE_TEST = "/";
    public static final String ROUTE_CHECK_USERNAME = "/checkUsername";
    public static final String ROUTE_LOGIN = "/login";
    public static final String ROUTE_REGISTER_USER = "/register";
    public static final String ROUTE_SYNC_FB_TOKEN = "/syncFbToken";
    public static final String ROUTE_CREATE_GROUP = "/createGroup";
    public static final String ROUTE_CREATE_GROUP_TRANSACTION = "/createGroupTransaction";
    public static final String ROUTE_GET_GROUPS = "/getGroups";
    public static final String ROUTE_GET_SIMILAR_USERS = "/getSimilarUsers";
    public static final String ROUTE_GET_TRANSACTIONS = "/getGroupTransactions";
    public static final String ROUTE_GET_CREDITS = "/getGroupBalances";


    public static final int CONNECT_TIMEOUT = 5000;
    public static final int RESPONSE_TIMEOUT = 15000;

    private static JSONObject readResponse(BufferedReader reader) throws JSONException {
        String line;
        StringBuffer buffer = new StringBuffer();

        try {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
        } catch (Exception e){
            Log.d(TAG, "Can't read response:" + e.toString());
        }
        String responseString = buffer.toString();
        JSONObject response = null;
        if (!responseString.isEmpty()) {
            response = new JSONObject(responseString);
        }
        return response;
    }

    public static JSONObject doGetRequest(String route) {
        HttpURLConnection connection = null;
        String requestURL = SERVER_URL + route;
        JSONObject response = new JSONObject();

        try {
            URL url = new URL(requestURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(RESPONSE_TIMEOUT);
            connection.connect();

            // get the server response
            InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedResponse = new BufferedReader(inputStream);
            response = readResponse(bufferedResponse);
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Connection timed out");
            try {
                response.put("statusCode", HttpURLConnection.HTTP_GATEWAY_TIMEOUT);
                response.put("message", "Connection timed out");
            } catch (JSONException jsonException) {
                Log.d(TAG, "JSON formatting exception on timeout");
            }

        } catch (IOException | JSONException e) {
            try {
                response.put("statusCode", HttpURLConnection.HTTP_INTERNAL_ERROR);
                response.put("message", "Internal server error");
            } catch (JSONException e1) {
                Log.d(TAG, "JSON formatting exception on server error");
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    public static JSONObject doPostRequest(String route, JSONObject args) {

        if (isEmulator()) {
            SERVER_URL = EMULATOR_SERVER_URL;
        }
        HttpURLConnection connection = null;
        String requestURL = SERVER_URL + route;
        JSONObject response = new JSONObject();

        try {
            // set up the connection
            URL url = new URL(requestURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(METHOD_POST);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(RESPONSE_TIMEOUT);
            connection.setDoOutput(true);

            String requestBody = args.toString();
            connection.setFixedLengthStreamingMode(requestBody.getBytes("UTF-8").length);
            connection.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.addRequestProperty("Accept", "application/json");

            // write the json object to the request and post
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(requestBody);
            writer.flush();
            writer.close();

            // get the server response
            InputStreamReader inputStream = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedResponse = new BufferedReader(inputStream);
            response = readResponse(bufferedResponse);
            inputStream.close();
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Connection timed out");
            try {
                response.put("statusCode", HttpURLConnection.HTTP_GATEWAY_TIMEOUT);
                response.put("message", "Connection timed out");
            } catch (JSONException jsonException) {
                Log.d(TAG, "JSON formatting exception on timeout");
            }

        } catch (IOException | JSONException e) {
            try {
                response.put("statusCode", HttpURLConnection.HTTP_INTERNAL_ERROR);
                response.put("message", "Internal server error");
                e.printStackTrace();
            } catch (JSONException e1) {
                Log.d(TAG, "JSON formatting exception on server error");
            }
        } finally {
            // it might be a good idea to add closures to writer and reader here
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    public static JSONObject makeRequest(String method, String route, JSONObject args) {

        if (isEmulator()) {
            SERVER_URL = EMULATOR_SERVER_URL;
        }
        HttpURLConnection connection = null;
        String requestURL = SERVER_URL + route;
        JSONObject response = new JSONObject();

        try {
            // set up the connection
            URL url = new URL(requestURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(METHOD_POST);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(RESPONSE_TIMEOUT);

            connection.addRequestProperty("Accept", "application/json");

            if (method.equals(METHOD_POST)) {
                connection.setDoOutput(true);

                String requestBody = args.toString();
                connection.setFixedLengthStreamingMode(requestBody.length());
                connection.addRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // write the json object to the request and post
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(requestBody);
                writer.flush();
                writer.close();
            } else {
                connection.connect();
            }

            // get the server response
            BufferedReader bufferedResponse = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            response = readResponse(bufferedResponse);
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Connection timed out");
            try {
                response.put("status_code", HttpURLConnection.HTTP_GATEWAY_TIMEOUT);
                response.put("message", "Connection timed out");
            } catch (JSONException jsonException) {
                Log.d(TAG, "JSON formatting exception on timeout");
            }

        } catch (IOException | JSONException e) {
            try {
                response.put("status_code", HttpURLConnection.HTTP_INTERNAL_ERROR);
                response.put("message", "Internal server error");
            } catch (JSONException e1) {
                Log.d(TAG, "JSON formatting exception on server error");
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    public static boolean isApiOn() {
        ApiRequest request = new ApiRequest(METHOD_GET, ROUTE_TEST, null, null);
        request.execute();
        JSONObject response = null;
        try {
            response = request.get(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
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
