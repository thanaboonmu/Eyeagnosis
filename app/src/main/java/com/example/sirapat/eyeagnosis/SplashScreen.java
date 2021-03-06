package com.example.sirapat.eyeagnosis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    sleep(1500);  // 1.5seconds of loading
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    SharedPreferences sp = getSharedPreferences("myjwt", Context.MODE_PRIVATE);
                    String token = sp.getString("token", "");
                    String username = sp.getString("username", "");
                    if (token.equals("") && username.equals("")) {
                        Intent i = new Intent(getApplicationContext(), SigninActivity.class);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    }
                    finish();
                }
            }
        };
        welcomeThread.start();
    }
}
