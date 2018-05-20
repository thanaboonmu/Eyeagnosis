package com.example.sirapat.eyeagnosis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.premnirmal.textcounter.CounterView;

import com.google.gson.JsonArray;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.roger.catloadinglibrary.CatLoadingView;


import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;
import org.w3c.dom.Text;


/**
 * Created by sirapat on 28/4/2018 AD.
 */

public class TrackActivity extends AppCompatActivity {

    private String IP;

    String tempLeftRes;
    String tempRightRes;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home: {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    if (tempLeftRes != null) {
                        i.putExtra("tempLeftRes", tempLeftRes);
                    }
                    if (tempRightRes != null) {
                        i.putExtra("tempRightRes", tempRightRes);
                    }
                    startActivity(i);
                    return true;
                }
                case R.id.navigation_camera: {
                    Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                    if (tempLeftRes != null) {
                        i.putExtra("tempLeftRes", tempLeftRes);
                    }
                    if (tempRightRes != null) {
                        i.putExtra("tempRightRes", tempRightRes);
                    }
                    startActivity(i);
                    return true;
                }
                case R.id.navigation_result: {
                    Intent i = new Intent(getApplicationContext(), Result.class);
                    if (tempLeftRes != null) {
                        i.putExtra("tempLeftRes", tempLeftRes);
                    }
                    if (tempRightRes != null) {
                        i.putExtra("tempRightRes", tempRightRes);
                    }
                    startActivity(i);
                    return true;
                }
                case R.id.navigation_track: {
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
                    Intent i = new Intent(TrackActivity.this, SigninActivity.class);
                    TrackActivity.this.startActivity(i);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(TrackActivity.this);
            builder.setTitle("Confirmation").setMessage("Sign out?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);
        setTitle("Tracking");
        TextView mSigninLink = (TextView) findViewById(R.id.link_signin);

        Config config = new Config();
        IP = config.IP;

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if (extras.getString("tempLeftRes") != null) {
                tempLeftRes = extras.getString("tempLeftRes");
            }
            if (extras.getString("tempRightRes") != null) {
                tempRightRes = extras.getString("tempRightRes");
            }
        } else {
            Log.e("Temp Res: ", "No temp res yet");
        }

        SharedPreferences sp = getSharedPreferences("myjwt", Context.MODE_PRIVATE);
        final String token = sp.getString("token", "");
        final String username = sp.getString("username", "");

        if(token.equals("") && username.equals("")) {
            mSigninLink.setText("Tap here to sign in");
            mSigninLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
                    startActivity(intent);
                }
            });
            Toast.makeText(TrackActivity.this, "Sign in to use this feature", Toast.LENGTH_LONG).show();
            return;
        }

        final CounterView counterView = (CounterView) findViewById(R.id.counterView);
        final ValueLineChart mCubicValueLineChart = (ValueLineChart) findViewById(R.id.cubiclinechart);
        final ValueLineSeries series = new ValueLineSeries();
        series.setColor(0xFF56B7F1);

        final CatLoadingView loading = new CatLoadingView();

        loading.show(getSupportFragmentManager(), "Getting data...");
        Ion.with(TrackActivity.this)
                .load("http://" + IP + ":8080/api/users/" + username)
                .setTimeout(10000)
                .setHeader("Authorization", "Bearer " + token)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        loading.dismiss();
                        if(result != null) {
                            int size = result.size();
                            float pos_num = 100.0f;
                            float before = 0;
                            float after = 0;
                            Log.e("result",result.toString());
                            for (int i = 0; i < size; i++) {
                                String possibility = result.get(i).getAsJsonObject().get("possibility").toString();
                                String disease = result.get(i).getAsJsonObject().get("disease").toString();
                                possibility = possibility.replace("\"", "");
                                Log.e(String.valueOf(i), possibility);
                                if(disease.equals("\"Healthy\"")) {
                                    pos_num = Float.parseFloat(possibility)*100;
                                } else {
                                    pos_num = 100f - Float.parseFloat(possibility)*100;
                                }
                                series.addPoint(new ValueLinePoint(String.valueOf(i), pos_num));
                                if(i == (size-2)) {
                                    before = pos_num;
                                } else if (i == (size - 1)) {
                                    after = pos_num;
                                }
                            }
                            float dif = after-before;
                            if(dif < 0) {
                                dif = Math.abs(dif);
                                counterView.setPrefix("-");
                                counterView.setSuffix("% worse");
                                counterView.setTextColor(Color.parseColor("#FF0000"));
                            }
                            counterView.setEndValue(dif);
                            counterView.start();
                            mCubicValueLineChart.addSeries(series);
                            mCubicValueLineChart.startAnimation();
                        } else {
                            loading.dismiss();
                            Toast.makeText(TrackActivity.this, "Server isn't running", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
