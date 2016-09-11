package br.com.fclug.financialaid;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;

public class LoginActivity extends AppCompatActivity {

    private static int LOGIN_INTENT = 1;
    private static int REGISTER_INTENT = 2;

    private SessionManager mSession;

    private EditText mUsername;
    private EditText mPassword;
    private Button mSignIn;
    private Button mSignUp;

    private int mIntent;
    private String usernameString;
    private String passwordString;

    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;

    private TextWatcher mWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            usernameString = mUsername.getText().toString().trim();
            passwordString = mPassword.getText().toString().trim();
            mSignIn.setEnabled(!usernameString.isEmpty() && !passwordString.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private ApiRequest.RequestCallback mLoginCallback = new ApiRequest.RequestCallback() {
        @Override
        public void onSuccess(JSONObject response) {
            String token = null;
            if (response != null) {
                try {
                    token = response.getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mProgressDialog.dismiss();
            if (token != null && !token.equals("")){
                // Creating user login session
                mSession.createLoginSession(usernameString, passwordString, token);

                // Start MainActivity
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();

            } else{
                mAlertDialog.setMessage("Username/password doesn't match");
                mAlertDialog.show();
            }
        }

        @Override
        public void onFailure() {

        }
    };

    ApiRequest.RequestCallback mRegisterCallback = new ApiRequest.RequestCallback() {
        @Override
        public void onSuccess(JSONObject response) {

        }

        @Override
        public void onFailure() {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSession = new SessionManager(getApplicationContext());

        mUsername = (EditText) findViewById(R.id.login_username);
        mPassword = (EditText) findViewById(R.id.login_password);
        mSignIn = (Button) findViewById(R.id.login_signin);
        mSignUp = (Button) findViewById(R.id.login_signup);

        mUsername.addTextChangedListener(mWatcher);
        mPassword.addTextChangedListener(mWatcher);
        mSignIn.setEnabled(false);

        mIntent = LOGIN_INTENT;

        mProgressDialog = new ProgressDialog(LoginActivity.this);
        mAlertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        mAlertDialog.setTitle("Login failed!");
        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mUsername.getText().clear();
                mPassword.getText().clear();
            }
        });

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressDialog.setTitle("Loading");
                mProgressDialog.setMessage("Wait while loading...");
                mProgressDialog.show();

                JSONObject args = new JSONObject();
                try {
                    args.put("username", usernameString);
                    args.put("password", passwordString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(mIntent == REGISTER_INTENT) {
                    new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_REGISTER_USER, args, mRegisterCallback).execute();
                }

                new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_LOGIN, args, mLoginCallback).execute();

            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = REGISTER_INTENT;
                mSignUp.setVisibility(View.GONE);
                mSignIn.setText(R.string.user_register);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(mIntent == LOGIN_INTENT) {
            super.onBackPressed();
        } else {
            mIntent = LOGIN_INTENT;
            mSignIn.setText(R.string.login_signin);
            mSignUp.setVisibility(View.VISIBLE);
        }
    }
}
