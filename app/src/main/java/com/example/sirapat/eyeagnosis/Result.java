package com.example.sirapat.eyeagnosis;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import org.json.JSONException;
import org.json.JSONObject;

public class Result extends AppCompatActivity {

    String leftResponse;
    String rightResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        TextView textView = (TextView) findViewById(R.id.resultTextView);
        DonutProgress donutProgress = (DonutProgress) findViewById(R.id.donut_progress);
        donutProgress.setFinishedStrokeWidth(40);
        donutProgress.setUnfinishedStrokeWidth(40);
        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            leftResponse = extras.getString("leftResponse");
            rightResponse = extras.getString("rightResponse");
            try {
                if (leftResponse != null) {
                    Log.e("leftResponse= ", leftResponse);
                    JSONObject leftResObj = new JSONObject(leftResponse);
                    JSONObject leftResult = leftResObj.getJSONObject("result");
                    String leftDisease = leftResult.getString("disease");
                    Double leftPossibility = leftResult.getDouble("possibility");
                    Integer leftSeverity = leftResult.getInt("severity");
                    Log.e("Disease: ", leftDisease);
                    Log.e("Possibility: ", String.valueOf(leftPossibility));
                    Log.e("Severity: ", String.valueOf(leftSeverity));
                    textView.setText(leftDisease);
                    donutProgress.setProgress((float)(leftPossibility*100));
                    if (leftPossibility > 0.5) {
                        donutProgress.setFinishedStrokeColor(Color.RED);
                        donutProgress.setUnfinishedStrokeColor(Color.GREEN);
                    }
                } else {
                    Log.e("LEFT", "NO DATA");
                }
                if (rightResponse != null) {
                    Log.e("rightResponse= ", rightResponse);
                    JSONObject rightResObj = new JSONObject(rightResponse);
                    JSONObject rightResult = rightResObj.getJSONObject("result");
                    String rightDisease = rightResult.getString("disease");
                    Double rightPossibility = rightResult.getDouble("possibility");
                    Integer rightSeverity = rightResult.getInt("severity");
                    Log.e("Disease: ", rightDisease);
                    Log.e("Possibility: ", String.valueOf(rightPossibility));
                    Log.e("Severity: ", String.valueOf(rightSeverity));
                } else {
                    Log.e("RIGHT", "NO DATA");
                }
            } catch(JSONException e) {
                Log.e("", "unexpected JSON exception", e);
            }
        } else {
            Log.e("ERROR", "Couldn't get result from HttpUpload.postExecute");
        }
    }
}