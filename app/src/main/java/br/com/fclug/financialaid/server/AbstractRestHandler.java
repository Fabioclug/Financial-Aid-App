package br.com.fclug.financialaid.server;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fabioclug on 2016-09-06.
 */
public abstract class AbstractRestHandler {
    private String TAG = "RestHandler";

//    protected JSONObject doRequest(String method, String route, JSONObject args) {
//        ApiRequest request = new ApiRequest(method, route, args);
//        request.execute();
//        JSONObject response = null;
//        try {
//            response = request.getResponse();
//        } catch (Exception e) {
//            Log.d(TAG, e.toString());
//        }
//        return response;
//    }

}
