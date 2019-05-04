package br.com.fclug.financialaid.server;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

/**
 * Created by Fabioclug on 2016-09-05.
 */

public class ApiRequest extends AsyncTask<Void, Void, JSONObject> {

    public interface RequestCallback {
        void onSuccess(JSONObject response) throws JSONException;
        void onFailure(int code);
    }

    private static final String TAG = "ApiRequest";

    private String mHttpMethod;
    private String mRoute;
    private JSONObject mArgs;
    private RequestCallback mCallback;

    public ApiRequest(String method, String route, JSONObject args, RequestCallback callback) {
        mHttpMethod = method;
        mRoute = route;
        mArgs = args;
        mCallback = callback;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        JSONObject response;
        if(mHttpMethod.equals(ServerUtils.METHOD_POST)) {
            response = ServerUtils.doPostRequest(mRoute, mArgs);
        } else {
            response = ServerUtils.doGetRequest(mRoute);
        }
        return response;
        //return ServerUtils.makeRequest(mHttpMethod, mRoute, mArgs);
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        super.onPostExecute(response);
        int statusCode = -1;
        Log.d("ApiRequest", mArgs.toString());
        try {
            Log.d(TAG, response.toString());
            statusCode = response.getInt("statusCode");
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (statusCode) {
            case HttpURLConnection.HTTP_OK:
                try {
                    mCallback.onSuccess(response);
                } catch (JSONException e) {
                    Log.d(TAG, e.toString());
                }
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                mCallback.onFailure(statusCode);
                break;
            default:
                Log.d(TAG, "Unexpected response code");
                break;

        }
    }

}
