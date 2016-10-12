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

public class RegisterActivity extends AppCompatActivity {

    private SessionManager mSession;

    private EditText mName;
    private EditText mUsername;
    private EditText mPassword;
    private EditText mPasswordRepeat;
    private Button mRegister;
    private AlertDialog mAlertDialog;

    private TextWatcher mWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mRegister.setEnabled(!mName.getText().toString().trim().isEmpty() &&
                    !mUsername.getText().toString().trim().isEmpty() &&
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

        mName = (EditText) findViewById(R.id.register_name);
        mUsername = (EditText) findViewById(R.id.register_username);
        mPassword = (EditText) findViewById(R.id.register_password);
        mPasswordRepeat = (EditText) findViewById(R.id.register_password_repeat);
        mRegister = (Button) findViewById(R.id.register_button);

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
                                public void onSuccess(JSONObject response) {
                                    progressDialog.dismiss();
                                    String token = null;
                                    try {
                                        token = response.getString("token");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    mSession.createLoginSession(username, password, token);
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
