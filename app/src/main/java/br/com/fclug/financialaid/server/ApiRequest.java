package br.com.fclug.financialaid.server;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by Fabioclug on 2016-09-05.
 */

public class ApiRequest extends AsyncTask<Void, Void, JSONObject> {

    public interface RequestCallback {
        void onSuccess(JSONObject response);
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
        JSONObject response = null;
        try {
            if(mHttpMethod.equals(ServerUtils.METHOD_POST)) {
                response = ServerUtils.doPostRequest(mRoute, mArgs);
            } else {
                response = ServerUtils.doGetRequest(mRoute);
            }

        } catch (IOException | JSONException e) {
            Log.d(TAG, "Impossible to complete request: " + e.toString());
        }
        return response;
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        super.onPostExecute(response);
        int statusCode = -1;
        try {
            Log.d(TAG, response.toString());
            statusCode = response.getInt("status_code");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ApiRequest", mArgs.toString());
        }
        switch (statusCode) {
            case HttpURLConnection.HTTP_OK:
                mCallback.onSuccess(response);
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                mCallback.onFailure(statusCode);
                break;
            default:
                break;

        }
    }

}
