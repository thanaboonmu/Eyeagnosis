package com.example.sirapat.eyeagnosis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by sirapat on 23/4/2018 AD.
 */

public class SigninActivity extends AppCompatActivity {

    private static final int SIGNUP_INTENT = 0;
    private String IP;

    private EditText mUsernameText;
    private EditText mPasswordText;
    private Button mSigninButton;
    private TextView mSignupLink;
    private ProgressDialog progressDialog;

    private int statusCode;
    private JSONObject signinResponse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Config config = new Config();
        IP = config.IP;

        setContentView(R.layout.activity_signin);

        mUsernameText = (EditText) findViewById(R.id.input_username);
        mPasswordText = (EditText) findViewById(R.id.input_password);
        mSigninButton = (Button) findViewById(R.id.btn_signin);
        mSignupLink = (TextView) findViewById(R.id.link_signup);

        mSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signin();
            }
        });

        mSignupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
//                startActivityForResult(intent, SIGNUP_INTENT);
            }
        });
    }

    private void signin() {
        if(!validate()) {
            onSigninFailed();
            return;
        }
        mSigninButton.setEnabled(false);

        progressDialog = new ProgressDialog(SigninActivity.this);
        progressDialog.setMessage("Sign in, Authenticating...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        delay(2000);

        callSigninAPI();

    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void callSigninAPI() {
        String credentials = mUsernameText.getText().toString() + ":" + mPasswordText.getText().toString();

        Ion.with(SigninActivity.this)
                .load("http://" + IP + ":8080/api/authenticate")
                .setHeader("Authorization", "Basic " + new String(Base64.encode(credentials.getBytes(),Base64.NO_WRAP)))
                .setBodyParameter("foo", "bar")
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        if (result != null) {
                            statusCode = result.getHeaders().code();
                            try {
                                signinResponse = new JSONObject(result.getResult().toString());
                                if(statusCode == 200) {
                                    String token = signinResponse.getString("token");
                                    String username = signinResponse.getString("message");
                                    Toast.makeText(SigninActivity.this, token, Toast.LENGTH_LONG).show();
                                    SharedPreferences sp = getSharedPreferences("myjwt", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("token", token);
                                    editor.putString("username", username);
                                    editor.apply();
                                    onSigninSuccess();
                                } else {
                                    String errMessage = signinResponse.getString("message");
                                    Toast.makeText(SigninActivity.this, errMessage, Toast.LENGTH_LONG).show();
                                    onSigninFailed();
                                }
                            } catch (JSONException err) {
                                Log.e("", "unexpected JSON exception", err);
                            }
                        } else {
                            Log.e("Sign in error: ", e.toString());
                            Toast.makeText(SigninActivity.this, "Could not connect to the server", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void delay(final int milliseconds) {
        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    sleep(milliseconds);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        welcomeThread.start();
    }

    public void onSigninSuccess() {
        mSigninButton.setEnabled(true);
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Intent i = new Intent(SigninActivity.this, MainActivity.class);
        SigninActivity.this.startActivity(i);

    }

    public void onSigninFailed() {
        Toast.makeText(getBaseContext(), "Sign in failed", Toast.LENGTH_LONG).show();
        mSigninButton.setEnabled(true);
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private boolean validate() {

        boolean valid = true;
        String username = mUsernameText.getText().toString();
        String password = mPasswordText.getText().toString();

        if(username.isEmpty()) {
            mUsernameText.setError("Enter a valid username");
            valid = false;
        } else {
            mUsernameText.setError(null);
            valid = true;
        }

        if(password.isEmpty() || password.length() < 3 || password.length() > 10) {
            mPasswordText.setError("Password should be between 3 and 10 characters");
            valid = false;
        } else {
            mPasswordText.setError(null);
            valid = true;
        }

        return valid;
    }
}
