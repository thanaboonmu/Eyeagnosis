package com.example.sirapat.eyeagnosis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    final private int NORMAL_MODE = 0;
    final private int RED_REFLECT_MODE = 1;
    private TextView mTextMessage;

    String tempLeftRes;
    String tempRightRes;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home: {
                    return true;
                }
                case R.id.navigation_camera: {
                    Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                    i.putExtra("mode", NORMAL_MODE);
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
                    Intent i = new Intent(getApplicationContext(), TrackActivity.class);
                    if (tempLeftRes != null) {
                        i.putExtra("tempLeftRes", tempLeftRes);
                    }
                    if (tempRightRes != null) {
                        i.putExtra("tempRightRes", tempRightRes);
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
                    Intent i = new Intent(MainActivity.this, SigninActivity.class);
                    MainActivity.this.startActivity(i);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Confirmation").setMessage("Sign out?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
        setTitle("Home");

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

        LottieAnimationView animationView = (LottieAnimationView) findViewById(R.id.animation_view);
        animationView.setAnimation("camera2.json");
        animationView.loop(true);
        animationView.playAnimation();

    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
    }
}
