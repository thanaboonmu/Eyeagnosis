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

    private String IP;

    private EditText mUsernameText;
    private EditText mPasswordText;
    private Button mSigninButton;
    private TextView mSignupLink;
    private TextView mSkipLink;
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
        mSkipLink = (TextView) findViewById(R.id.link_skip);

        mSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signin();
            }
        });

        mSignupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

        mSkipLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void signin() {
        if(!validate()) {
            return;
        }
        mSigninButton.setEnabled(false);

        progressDialog = new ProgressDialog(SigninActivity.this);
        progressDialog.setMessage("Sign in, Authenticating...");
        progressDialog.setCancelable(false);
        progressDialog.show();

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
                            onSigninFailed();
                        }
                    }
                });
    }


    public void onSigninSuccess() {
        mSigninButton.setEnabled(true);
        Thread delayProgressThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    sleep(1500);  // 1.5 seconds of loading
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    progressDialog.dismiss();
                    Intent i = new Intent(SigninActivity.this, MainActivity.class);
                    SigninActivity.this.startActivity(i);
                }
            }
        };
        if (progressDialog.isShowing()) {
            delayProgressThread.start();
        }
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

        if(username.isEmpty() || username.length() < 4 || username.length() > 10) {
            mUsernameText.setError("Enter a valid username");
            valid = false;
        } else {
            mUsernameText.setError(null);
        }

        if(password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordText.setError("Password should be between 4 and 10 characters");
            valid = false;
        } else {
            mPasswordText.setError(null);
        }

        return valid;
    }
}
