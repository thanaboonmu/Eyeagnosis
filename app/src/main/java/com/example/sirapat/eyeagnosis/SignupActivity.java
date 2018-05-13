package com.example.sirapat.eyeagnosis;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {

    private String IP;

    private EditText mUsernameText;
    private EditText mPasswordText;
    private EditText mConfirmPasswordText;
    private EditText mAgeText;
    private Button mSignupButton;
    private TextView mSigninLink;
    private ProgressDialog progressDialog;

    private String gender = "male";

    private int statusCode;
    private JSONObject signupResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Config config = new Config();
        IP = config.IP;

        setContentView(R.layout.activity_signup);
        mUsernameText = (EditText) findViewById(R.id.input_username);
        mPasswordText = (EditText) findViewById(R.id.input_password);
        mConfirmPasswordText = (EditText) findViewById(R.id.input_confirm_password);
        mAgeText = (EditText) findViewById(R.id.input_age);
        mSignupButton = (Button) findViewById(R.id.btn_signup);
        mSigninLink = (TextView) findViewById(R.id.link_signin);

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });

        mSigninLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_male:
                if (checked)
                    gender = "male";
                    break;
            case R.id.radio_female:
                if (checked)
                    gender = "female";
                    break;
        }
    }

    private void signup() {
        if(!validate()) {
            return;
        }
        mSignupButton.setEnabled(false);

        progressDialog = new ProgressDialog(SignupActivity.this);
        progressDialog.setMessage("Sign up, Registering...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        callSignupAPI();
    }

    public void callSignupAPI() {

        String credentials = mUsernameText.getText().toString() + ":" + mPasswordText.getText().toString();

        JsonObject requestObj = new JsonObject();
        requestObj.addProperty("age", mAgeText.getText().toString());
        requestObj.addProperty("gender",gender);

        Ion.with(SignupActivity.this)
                .load("http://" + IP + ":8080/api/users")
                .setHeader("Authorization", "Basic " + new String(Base64.encode(credentials.getBytes(),Base64.NO_WRAP)))
                .setHeader("Content-Type", "application/json")
                .setJsonObjectBody(requestObj)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        if (result != null) {
                            statusCode = result.getHeaders().code();
                            try {
                                signupResponse = new JSONObject(result.getResult().toString());
                                if(statusCode == 201) {
                                    String successMessage = signupResponse.getString("message");
                                    Toast.makeText(SignupActivity.this, successMessage, Toast.LENGTH_LONG).show();
                                    onSignupSuccess();
                                } else {
                                    String errMessage = signupResponse.getString("message");
                                    Toast.makeText(SignupActivity.this, errMessage, Toast.LENGTH_LONG).show();
                                    onSignupFailed();
                                }
                            } catch (JSONException err) {
                                Log.e("", "unexpected JSON exception", err);
                            }
                        } else {
                            Log.e("Sign in error: ", e.toString());
                            Toast.makeText(SignupActivity.this, "Could not connect to the server", Toast.LENGTH_SHORT).show();
                            onSignupFailed();
                        }
                    }
                });
    }


    public void onSignupSuccess() {
        mSignupButton.setEnabled(true);
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
                    Intent i = new Intent(SignupActivity.this, SigninActivity.class);
                    startActivity(i);
                }
            }
        };
        if (progressDialog.isShowing()) {
            delayProgressThread.start();
        }
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_LONG).show();
        mSignupButton.setEnabled(true);
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private boolean validate() {

        boolean valid = true;
        String username = mUsernameText.getText().toString();
        String password = mPasswordText.getText().toString();
        String confirm_password = mConfirmPasswordText.getText().toString();
        String age = mAgeText.getText().toString();

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

        if(confirm_password.isEmpty() || !confirm_password.equals(password)) {
            mConfirmPasswordText.setError("Confirm password should match password");
            valid = false;
        } else {
            mConfirmPasswordText.setError(null);
        }

        if(age.isEmpty()) {
            mAgeText.setError("Enter a valid age");
            valid = false;
        } else {
            try {
                int age_int = Integer.parseInt(age);
                if(age_int <= 0 || age_int > 100) {
                    mAgeText.setError("Age should be between 1-100");
                    valid = false;
                } else {
                    mAgeText.setError(null);
                }
            } catch (NumberFormatException e) {
                mAgeText.setError("Please enter a valide age");
                Log.e("Error", "can not parse age to int");
                valid = false;
            }
        }

        if(gender.isEmpty()) {
            Log.e("Error", "no gender found");
            valid = false;
        } else if(!gender.equals("male") && !gender.equals("female")) {
            Log.e("Error", "Gender is either male or female");
            valid = false;
        }

        return valid;
    }
}
