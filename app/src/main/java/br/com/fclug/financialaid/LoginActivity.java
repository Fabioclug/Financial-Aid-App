package br.com.fclug.financialaid;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText mUsername;
    private EditText mPassword;
    private Button mSignIn;
    private Button mSignUp;
    private TextView mSkipLogin;

    private String usernameString;
    private String passwordString;
    private JSONObject requestArgs;

    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;

    private TextWatcher mWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            usernameString = mUsername.getText().toString();
            passwordString = mPassword.getText().toString();
            mSignIn.setEnabled(!usernameString.trim().isEmpty() && !passwordString.trim().isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private ApiRequest.RequestCallback mLoginCallback = new ApiRequest.RequestCallback() {
        @Override
        public void onSuccess(JSONObject response) throws JSONException {
            String token = null;
            String name = null;
            if (response != null) {
                token = response.getString("token");
                name = response.getString("name");
            }
            mProgressDialog.dismiss();
            // Creating user login session
            SessionManager.createLoginSession(LoginActivity.this, usernameString, name, passwordString, token);
            SessionManager.sendFbTokenToServer(LoginActivity.this);

            // Start MainActivity
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }

        @Override
        public void onFailure(int code) {
            mProgressDialog.dismiss();
            switch (code) {
                case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                    Toast.makeText(LoginActivity.this, "Server is unavailable", Toast.LENGTH_SHORT).show();
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    mAlertDialog.setMessage("Username and password do not match");
                    mAlertDialog.show();
                    break;
                case HttpURLConnection.HTTP_INTERNAL_ERROR:
                    mAlertDialog.setMessage("Unable to connect to server");
                    mAlertDialog.show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername = (EditText) findViewById(R.id.login_username);
        mPassword = (EditText) findViewById(R.id.login_password);
        mSignIn = (Button) findViewById(R.id.login_signin);
        mSignUp = (Button) findViewById(R.id.login_signup);
        mSkipLogin = (TextView) findViewById(R.id.login_skip);

        mUsername.addTextChangedListener(mWatcher);
        mPassword.addTextChangedListener(mWatcher);
        mSignIn.setEnabled(false);


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
                mProgressDialog.setMessage("Authenticating...");
                mProgressDialog.show();

                requestArgs = new JSONObject();
                try {
                    requestArgs.put("username", usernameString);
                    requestArgs.put("password", passwordString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_LOGIN, requestArgs, mLoginCallback).execute();

            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mSkipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionManager.skipLogin(LoginActivity.this);
            }
        });

    }
}
