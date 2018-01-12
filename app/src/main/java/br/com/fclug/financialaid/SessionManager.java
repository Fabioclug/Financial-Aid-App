package br.com.fclug.financialaid;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by Fabioclug on 2016-09-07.
 */

public class SessionManager {

    private static final String PREF_NAME = "financial_aid";

    // Shared Preferences keys
    private static final String IS_LOGGED = "logged";
    private static final String SKIPPED = "skipped";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_NAME = "name";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_TOKEN = "token";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void requestLogin(String username, String password) {
        //TODO: the API request and call createLoginSession
    }

    public void requestLogin(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        String username = preferences.getString(KEY_USERNAME, null);
        String password = preferences.getString(KEY_PASSWORD, null);
        if(username != null && password != null) {
            requestLogin(username, password);
        }
    }

    /**
     * Create login session
     * */
    public static void createLoginSession(Context context, String username, String name, String password, String token){
        // Storing login values in shared mSharedPreferences
        Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(IS_LOGGED, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(SKIPPED, false);
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public static void checkLogin(Context context) {
        if(!hasSkipped(context) && !isLoggedIn(context)) {
            Intent i = new Intent(context, LoginActivity.class);

            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    /**
     * Get stored session data
     * @return a HashMap with the user information
     */
    public static HashMap<String, String> getUserDetails(Context context) {
        HashMap<String, String> user = new HashMap<String, String>();
        SharedPreferences preferences = getSharedPreferences(context);
        user.put(KEY_USERNAME, preferences.getString(KEY_USERNAME, null));
        user.put(KEY_NAME, preferences.getString(KEY_NAME, null));
        user.put(KEY_TOKEN, preferences.getString(KEY_TOKEN, null));

        return user;
    }

    public static String getToken(Context context) {
        return getSharedPreferences(context).getString(KEY_TOKEN, null);
    }

    /**
     * Clear session details
     * */
    public static void logoutUser(Context context) {
        // Clear all data from Shared Preferences
        Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();

        // Redirect user to Login Activity
        Intent i = new Intent(context, LoginActivity.class);

        // Close all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static void skipLogin(Context context) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(SKIPPED, true);
        editor.commit();
        Intent i = new Intent(context, MainActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    /**
     * Check if the user is logged in
     * @return a flag indicating if the user is logged in
     */
    public static boolean isLoggedIn(Context context) {
        return getSharedPreferences(context).getBoolean(IS_LOGGED, false);
    }

    /**
     * Check if the user has skipped login before
     * @return a flag indicating if the user skipped login
     */
    public static boolean hasSkipped(Context context) {
        return getSharedPreferences(context).getBoolean(SKIPPED, false);
    }
}