package com.example.sirapat.eyeagnosis;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
                case R.id.navigation_result:
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
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

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

//        Button normalModeButton = (Button) findViewById(R.id.normalModeButton);
//        normalModeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(view.getContext(), CameraActivity.class);
//                i.putExtra("mode", NORMAL_MODE);
//                startActivity(i);
//            }
//        });
//        Button redReflectModeButton = (Button) findViewById(R.id.redReflectModeButton);
//        redReflectModeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(view.getContext(), CameraActivity.class);
//                i.putExtra("mode", RED_REFLECT_MODE);
//                startActivity(i);
//            }
//        });
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
