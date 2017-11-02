package br.com.fclug.financialaid;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;

public class RegisterActivity extends AppCompatActivity {

    private SessionManager mSession;
    private HashMap<String, String> mUserData;

    private EditText mName;
    private EditText mUsername;
    private EditText mPassword;
    private EditText mPasswordRepeat;
    private Button mRegister;
    private ImageView mFeedbackImage;
    private AlertDialog mAlertDialog;

    private TextWatcher mWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String username = mUsername.getText().toString().trim();
            boolean usernameOk = false;
            if (!username.isEmpty()) {
                try {
                    ApiRequest.RequestCallback callback = new ApiRequest.RequestCallback() {
                        @Override
                        public void onSuccess(JSONObject response) throws JSONException {
                            boolean exists = response.getJSONArray("result").getJSONObject(0).getInt("existing") == 1;
                            if (exists) {
                                mFeedbackImage.setImageResource(android.R.drawable.ic_delete);
                            } else {
                                Drawable confirm = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_confirm);
                                confirm.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.transaction_type_credit), PorterDuff.Mode.SRC_ATOP);
                                mFeedbackImage.setImageDrawable(confirm);
                            }
                            mFeedbackImage.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFailure(int code) {

                        }
                    };

                    JSONObject args = new JSONObject();
                    args.put("username", username);
                    Log.d("REgister", args.toString());

                    new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_CHECK_USERNAME, args, callback).execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                mFeedbackImage.setVisibility(View.GONE);
            }
            mRegister.setEnabled(!mName.getText().toString().trim().isEmpty() &&
                    !username.isEmpty() &&
                    !mPassword.getText().toString().trim().isEmpty() &&
                    !mPasswordRepeat.getText().toString().trim().isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        mSession = new SessionManager(RegisterActivity.this);
        mUserData = mSession.getUserDetails();

        mName = (EditText) findViewById(R.id.register_name);
        mUsername = (EditText) findViewById(R.id.register_username);
        mPassword = (EditText) findViewById(R.id.register_password);
        mPasswordRepeat = (EditText) findViewById(R.id.register_password_repeat);
        mRegister = (Button) findViewById(R.id.register_button);
        mFeedbackImage = (ImageView) findViewById(R.id.register_feedback);

        mName.addTextChangedListener(mWatcher);
        mUsername.addTextChangedListener(mWatcher);
        mPassword.addTextChangedListener(mWatcher);
        mPasswordRepeat.addTextChangedListener(mWatcher);

        final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setTitle("Registering");
        progressDialog.setMessage("Communicating with server...");

        mAlertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
        mAlertDialog.setTitle("Registration failed!");
        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mUsername.getText().clear();
                mPassword.getText().clear();
                mPasswordRepeat.getText().clear();
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                final String username = mUsername.getText().toString();
                final String password = mPassword.getText().toString();
                String passwordRepeat = mPasswordRepeat.getText().toString();
                if (password.equals(passwordRepeat)) {
                    JSONObject args = new JSONObject();
                    try {
                        args.put("name", mName.getText().toString());
                        args.put("username", username);
                        args.put("password", password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ApiRequest registerRequest = new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_REGISTER_USER, args,
                            new ApiRequest.RequestCallback() {
                                @Override
                                public void onSuccess(JSONObject response) throws JSONException {
                                    progressDialog.dismiss();
                                    String token = response.getString("token");
                                    mSession.createLoginSession(username, mName.getText().toString(), password, token);
                                    Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }

                                @Override
                                public void onFailure(int code) {
                                    progressDialog.dismiss();
                                }
                            });
                    registerRequest.execute();
                } else {
                    mAlertDialog.setMessage("Passwords don't match!");
                    mAlertDialog.show();
                }
            }
        });
    }
}
