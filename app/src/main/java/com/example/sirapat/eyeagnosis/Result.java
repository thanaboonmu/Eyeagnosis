package com.example.sirapat.eyeagnosis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.github.lzyzsd.circleprogress.DonutProgress;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class Result extends AppCompatActivity {

    final private int NORMAL_MODE = 0;
    String leftResponse;
    String rightResponse;
    String leftDisease = "Unknown";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home: {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    if (leftResponse != null) {
                        i.putExtra("tempLeftRes",leftResponse);
                    }
                    if (rightResponse != null) {
                        i.putExtra("tempRightRes", rightResponse);
                    }
                    startActivity(i);
                    return true;
                }
                case R.id.navigation_camera: {
                    Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                    i.putExtra("mode", NORMAL_MODE);
                    if (leftResponse != null) {
                        i.putExtra("tempLeftRes",leftResponse);
                    }
                    if (rightResponse != null) {
                        i.putExtra("tempRightRes", rightResponse);
                    }
                    startActivity(i);
                    return true;
                }
                case R.id.navigation_result: {
                    return true;
                }
                case R.id.navigation_track: {
                    Intent i = new Intent(getApplicationContext(), TrackActivity.class);
                    if (leftResponse != null) {
                        i.putExtra("tempLeftRes", leftResponse);
                    }
                    if (rightResponse != null) {
                        i.putExtra("tempRightRes", rightResponse);
                    }
                    startActivity(i);
                    return true;
                }
            }
            return false;
        }

    };

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        SharedPreferences sp = getSharedPreferences("myjwt", Context.MODE_PRIVATE);
        // if there is token then show sign out button
        if (!sp.getString("token","").equals("") && !sp.getString("username","").equals("")) {
            getMenuInflater().inflate(R.menu.signout, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    SharedPreferences sp = getSharedPreferences("myjwt", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("token", "");
                    editor.putString("username", "");
                    editor.apply();
                    Intent i = new Intent(Result.this, SigninActivity.class);
                    Result.this.startActivity(i);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    // handle button activities
    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();

        if (id == R.id.btn_signout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Result.this);
            builder.setTitle("Confirmation").setMessage("Sign out?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setTitle("Result");

        Bundle extras0 = getIntent().getExtras();
        if(extras0 != null) {
            leftResponse = extras0.getString("tempLeftRes");
            rightResponse = extras0.getString("tempRightRes");
        } else {
            Log.e("Temp response: ", "No temp Result yet");
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        LottieAnimationView animationView = (LottieAnimationView) findViewById(R.id.animation_view_res);
        animationView.loop(false);

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
            if (leftResponse == null) {
                leftResponse = extras.getString("leftResponse");
            }
            if (rightResponse == null) {
                rightResponse = extras.getString("rightResponse");
            }
        } else {
            Log.e("ERROR", "Couldn't get result from HttpUpload.postExecute");
        }
        try {
            if (leftResponse != null) {
                Log.e("leftResponse= ", leftResponse);
                JSONObject leftResObj = new JSONObject(leftResponse);
                JSONObject leftResult = leftResObj.getJSONObject("result");
                leftDisease = leftResult.getString("disease");
                Double leftPossibility = leftResult.getDouble("possibility");
                String leftRecommendation = leftResult.getString("recommendation");

                Log.e("Disease: ", leftDisease);
                Log.e("Possibility: ", String.valueOf(leftPossibility));
                Log.e("Recommendation: ", leftRecommendation);
                leftDiseaseView.setText(leftDisease);
                leftProgress.setProgress((float)(leftPossibility*100));

                if (leftDisease.equals("Healthy")) {
                    animationView.setAnimation("good.json");
                    leftProgress.setFinishedStrokeColor(Color.GREEN);
                    leftProgress.setUnfinishedStrokeColor(Color.RED);
                } else {
                    animationView.setAnimation("warning.json");
                    leftProgress.setFinishedStrokeColor(Color.RED);
                    leftProgress.setUnfinishedStrokeColor(Color.GREEN);
                }
                animationView.playAnimation();
                leftRec.setText(leftRecommendation);
            } else {
                Log.e("LEFT", "NO DATA");
            }
            if (rightResponse != null) {
                Log.e("rightResponse= ", rightResponse);
                JSONObject rightResObj = new JSONObject(rightResponse);
                JSONObject rightResult = rightResObj.getJSONObject("result");
                String rightDisease = rightResult.getString("disease");
                Double rightPossibility = rightResult.getDouble("possibility");
                String rightRecommendation = rightResult.getString("recommendation");
                Log.e("Disease: ", rightDisease);
                Log.e("Possibility: ", String.valueOf(rightPossibility));
                Log.e("Recommendation: ", rightRecommendation);
                rightDiseaseView.setText(rightDisease);
                rightProgress.setProgress((float)(rightPossibility*100));
                if (rightDisease.equals("Healthy")) {
                    animationView.setAnimation("good.json");
                    if(!leftDisease.equals("Healthy") && !leftDisease.equals("Unknown")) {
                        animationView.setAnimation("warning.json");
                    }
                    rightProgress.setFinishedStrokeColor(Color.GREEN);
                    rightProgress.setUnfinishedStrokeColor(Color.RED);
                } else {
                    animationView.setAnimation("warning.json");
                    rightProgress.setFinishedStrokeColor(Color.RED);
                    rightProgress.setUnfinishedStrokeColor(Color.GREEN);
                }
                animationView.playAnimation();
                rightRec.setText(rightRecommendation);
            } else {
                Log.e("RIGHT", "NO DATA");
            }
        } catch(JSONException e) {
            Log.e("", "unexpected JSON exception", e);
        }
    }
}
