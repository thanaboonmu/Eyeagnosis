package com.example.sirapat.eyeagnosis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Result extends AppCompatActivity {

    String response = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            response = extras.getString("response");
            Log.e("response=", response);
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText(response);

        } else {
            Log.e("ERROR", "Couldn't get result from HttpUpload.postExecute");
        }
    }
}
