package br.com.fclug.financialaid

import android.content.Context
import android.content.Intent
import android.util.Log
import br.com.fclug.financialaid.server.ApiRequest
import br.com.fclug.financialaid.server.ApiRequest.RequestCallback
import br.com.fclug.financialaid.server.ServerUtils
import br.com.fclug.financialaid.utils.TAG
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Created by Fabioclug on 2016-09-07.
 */

const val PREF_NAME = "financial_aid"

// Shared Preferences keys
const val KEY_USERNAME = "username"
const val KEY_NAME = "name"
const val KEY_PASSWORD = "password"
const val KEY_TOKEN = "token"
const val KEY_FB_TOKEN = "fb_token"
const val IS_LOGGED = "logged"
const val SKIPPED = "skipped"

class SessionManager(val context: Context) {

    private val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getUsername(): String = preferences.getString(KEY_USERNAME, "") ?: ""

    fun getName(): String = preferences.getString(KEY_NAME, "") ?: ""

    fun getToken(): String = preferences.getString(KEY_TOKEN, "") ?: ""

    fun getFbToken(): String = preferences.getString(KEY_FB_TOKEN, "") ?: ""

    /**
     * Create login session
     */
    fun createLoginSession(username: String, name: String, password: String, token: String) {
        // Storing login values in shared preferences
        val editor = preferences.edit()
        editor.putBoolean(IS_LOGGED, true)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_PASSWORD, password)
        editor.putString(KEY_TOKEN, token)
        editor.putBoolean(SKIPPED, false)
        editor.commit()
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    fun checkLogin() {
        if (!hasSkipped() && !isLoggedIn()) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    /**
     * Clear session details
     */
    fun logoutUser() {
        // Clear all data from Shared Preferences
        val editor = preferences.edit()
        editor.clear()
        editor.commit()
        // Redirect user to Login Activity
        val intent = Intent(context, LoginActivity::class.java)
        // Add flags to close all the Activities and start new one
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun skipLogin() {
        val editor = preferences.edit()
        editor.putBoolean(SKIPPED, true)
        editor.commit()
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * Check if the user is logged in
     * @return a flag indicating if the user is logged in
     */
    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(IS_LOGGED, false)
    }

    /**
     * Check if the user has skipped login before
     * @return a flag indicating if the user skipped login
     */
    fun hasSkipped(): Boolean {
        return preferences.getBoolean(SKIPPED, false)
    }

    fun updateFbToken(fbToken: String) {
        val editor = preferences.edit()
        editor.putString(KEY_FB_TOKEN, fbToken)
        editor.commit()
        Log.d(TAG, "updating fbToken: $fbToken")
        sendFbTokenToServer()
    }

    fun sendFbTokenToServer() {
        if (isLoggedIn()) {
            val token = getToken()
            val fbToken = getFbToken()
            val request = JSONObject()
            try {
                request.put("token", token)
                Log.d(TAG, "fbToken: $fbToken")
                request.put("fb_token", fbToken)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_SYNC_FB_TOKEN, request, object : RequestCallback {
                @Throws(JSONException::class)
                override fun onSuccess(response: JSONObject) {
                    Log.d(TAG, response.toString())
                    Log.d(TAG, "Token synced with server")
                }

                override fun onFailure(code: Int) {
                    Log.d(TAG, "Couldn't sync token")
                }
            }).execute()
        }
    }

    fun requestLogin(username: String, password: String) {
        val requestArgs = JSONObject()
        try {
            requestArgs.put("username", username)
            requestArgs.put("password", password)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        //new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_LOGIN, requestArgs, mLoginCallback).execute();
        //TODO: the API request and call createLoginSession
    }

    fun requestLogin() {
        val username = preferences.getString(KEY_USERNAME, null)
        val password = preferences.getString(KEY_PASSWORD, null)
        if (username != null && password != null) {
            requestLogin(username, password)
        }
    }

}