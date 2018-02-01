package com.example.sirapat.eyeagnosis;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class Result extends AppCompatActivity {

    String leftResponse;
    String rightResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        DonutProgress leftProgress = (DonutProgress) findViewById(R.id.leftDonut);
        leftProgress.setFinishedStrokeWidth(40);
        leftProgress.setUnfinishedStrokeWidth(40);
        DonutProgress rightProgress = (DonutProgress) findViewById(R.id.rightDonut);
        rightProgress.setFinishedStrokeWidth(40);
        rightProgress.setUnfinishedStrokeWidth(40);
        TextView leftDiseaseView = (TextView) findViewById(R.id.leftDisease);
        TextView rightDiseaseView = (TextView) findViewById(R.id.rightDisease);
        TextView leftRec = (TextView) findViewById(R.id.leftRec);
        TextView rightRec = (TextView) findViewById(R.id.rightRec);

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
//                    Integer leftSeverity = leftResult.getInt("severity");
                    Log.e("Disease: ", leftDisease);
                    Log.e("Possibility: ", String.valueOf(leftPossibility));
                    leftDiseaseView.setText(leftDisease);
                    leftProgress.setProgress((float)(leftPossibility*100));

                    if (leftDisease == "Healthy") {
                        leftProgress.setFinishedStrokeColor(Color.GREEN);
                        leftProgress.setUnfinishedStrokeColor(Color.RED);
                        leftRec.setText("Congratulations ! Your left eye is healthy.");
                    } else {
                        leftProgress.setFinishedStrokeColor(Color.RED);
                        leftProgress.setUnfinishedStrokeColor(Color.GREEN);
                        leftRec.setText("Danger! You have to meet a doctor as soon as possible.");
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
//                    Integer rightSeverity = rightResult.getInt("severity");
                    Log.e("Disease: ", rightDisease);
                    Log.e("Possibility: ", String.valueOf(rightPossibility));
                    rightDiseaseView.setText(rightDisease);
                    rightProgress.setProgress((float)(rightPossibility*100));
                    if (rightDisease == "Healthy") {
                        rightProgress.setFinishedStrokeColor(Color.GREEN);
                        rightProgress.setUnfinishedStrokeColor(Color.RED);
                        rightRec.setText("Congratulations ! Your left eye is healthy.");
                    } else {
                        rightProgress.setFinishedStrokeColor(Color.RED);
                        rightProgress.setUnfinishedStrokeColor(Color.GREEN);
                        rightRec.setText("Danger! You have to meet a doctor as soon as possible.");
                    }
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
