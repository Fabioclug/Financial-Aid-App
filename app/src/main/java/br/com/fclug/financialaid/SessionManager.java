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

    private SharedPreferences mSharedPreferences;
    private Context mContext;

    private static final String PREF_NAME = "financial_aid";

    // Shared Preferences keys
    private static final String IS_LOGGED = "logged";
    private static final String SKIPPED = "skipped";
    public static final String KEY_NAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_TOKEN = "token";

    public SessionManager(Context context){
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void requestLogin(String username, String password) {
        //TODO: the API request and call createLoginSession
    }

    public void requestLogin() {
        String username = mSharedPreferences.getString(KEY_NAME, null);
        String password = mSharedPreferences.getString(KEY_PASSWORD, null);
        if(username != null && password != null) {
            requestLogin(username, password);
        }
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String name, String password, String token){
        // Storing login values in shared mSharedPreferences
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(IS_LOGGED, true);
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
    public void checkLogin(){
        if(!this.hasSkipped() && !this.isLoggedIn()) {
            Intent i = new Intent(mContext, LoginActivity.class);

            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
        }
    }

    /**
     * Get stored session data
     * @return a HashMap with the user information
     */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();

        user.put(KEY_NAME, mSharedPreferences.getString(KEY_NAME, null));
        user.put(KEY_TOKEN, mSharedPreferences.getString(KEY_TOKEN, null));

        return user;
    }

    public String getToken() {
        return mSharedPreferences.getString(KEY_TOKEN, null);
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clear all data from Shared Preferences
        Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();

        // Redirect user to Login Activity
        Intent i = new Intent(mContext, LoginActivity.class);

        // Close all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
    }

    public void skipLogin() {
        Editor editor = mSharedPreferences.edit();
        editor.putBoolean(SKIPPED, true);
        editor.commit();
        Intent i = new Intent(mContext, MainActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
    }

    /**
     * Check if the user is logged in
     * @return a flag indicating if the user is logged in
     */
    public boolean isLoggedIn(){
        return mSharedPreferences.getBoolean(IS_LOGGED, false);
    }

    /**
     * Check if the user has skipped login before
     * @return a flag indicating if the user skipped login
     */
    public boolean hasSkipped() {
        return mSharedPreferences.getBoolean(SKIPPED, false);
    }
}