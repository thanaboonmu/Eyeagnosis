package com.example.sirapat.eyeagnosis;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.abs;

public class CameraActivity extends AppCompatActivity implements SensorEventListener {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 0;
    final private int NORMAL_MODE = 0;
    final private int RED_REFLECT_MODE = 1;
    final private int LEFT_SIDE = 0;
    final private int RIGHT_SIDE = 1;

    private int tag = 1;
    private Uri uri = null;
    private int diagnoseMode = -1;
    private Sensor mLight = null;
    private SensorManager mSensorManager = null;
    private float previousSensorValue = 0;
    private Toast sensorToast = null;
    private IconRoundCornerProgressBar lightBar = null;
    private ImageView leftImage = null;
    private ImageView rightImage = null;
    private int eyeSide = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            diagnoseMode = extras.getInt("mode");
            if(diagnoseMode == -1) {
                Log.e("ERROR","Mode = -1, something went wrong");
            }
        } else {
            Log.e("ERROR", "Couldn't get mode from main activity");
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lightBar = (IconRoundCornerProgressBar) findViewById(R.id.lightProgressBar);
        TextView modeText = (TextView) findViewById(R.id.modeTextView);
        if(diagnoseMode == NORMAL_MODE) {
            modeText.setText("NORMAL MODE: Detecting Pinguecula / Pterygium [Flash is recommended]");
            modeText.setTextColor(Color.parseColor("#50C878")); // GREEN
        } else if(diagnoseMode == RED_REFLECT_MODE) {
            modeText.setText("RED REFLECT MODE: Detecting Cataract / Retinoblastoma {Environment should be dark and Flash is required}");
            modeText.setTextColor(Color.parseColor("#FF0000")); // RED
        }
        leftImage = (ImageView) findViewById(R.id.leftImageView);
        rightImage = (ImageView) findViewById(R.id.rightImageView);
        ImageButton leftButton = (ImageButton) findViewById(R.id.leftImageButton);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eyeSide = LEFT_SIDE;
                openCamera(view);
            }
        });
        ImageButton rightButton = (ImageButton) findViewById(R.id.rightImageButton);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eyeSide = RIGHT_SIDE;
                openCamera(view);
            }
        });

        // For Android 7+ (Nougat) File system
        if(Build.VERSION.SDK_INT >= 24) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
    }

    public void openCamera(View view) {
        checkUserPermissions();
    }

    // For Android 6+ Marshmallow Permission
    void checkUserPermissions(){
        if (Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return ;
            }
        }

        takePicture();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    // Permission Denied
                    Toast.makeText( this,"PERMISSION DENIED (WRITE_EXTERNAL_STORAGE)" , Toast.LENGTH_SHORT)
                            .show();
                    Log.e("ERROR", "PERMISSION DENIED");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void takePicture() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timestamp + ".jpg";
        File dir = new File(Environment.getExternalStorageDirectory(), "DCIM/eyeagnosis");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        dir = new File(dir, "/" + imageFileName);
        uri = Uri.fromFile(dir);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, tag);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == tag && resultCode == RESULT_OK) {
            try {
                // put the saved image into gallery
                Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(uri);
                sendBroadcast(mediaScanIntent);
//                Bundle b = data.getExtras();
//                Bitmap img = (Bitmap) b.get("data");
//                imageView.setImageBitmap(img);
                // load the saved image
                getContentResolver().notifyChange(uri, null);
                ContentResolver cr = getContentResolver();
                Bitmap img = MediaStore.Images.Media.getBitmap(cr, uri);

//                Matrix mat = new Matrix(); //
//                mat.postRotate((-90)); // only for sss8 (temp solution)
//                img = Bitmap.createBitmap(img, 0,0, img.getWidth(), img.getHeight(), mat, true); //

                Paint myPaint = new Paint();
                myPaint.setColor(Color.YELLOW);
                myPaint.setStyle(Paint.Style.STROKE);
                myPaint.setStrokeWidth(10);

                Bitmap tempBitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.RGB_565);
                Canvas tempCanvas = new Canvas(tempBitmap);
                tempCanvas.drawBitmap(img, 0, 0, null);

                FaceDetector faceDetector = new
                        FaceDetector.Builder(getApplicationContext())
                        .setTrackingEnabled(false)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
//                        .setMode(FaceDetector.FAST_MODE)
                        .build();
                if(!faceDetector.isOperational()){
                    Log.e("ERROR","Couldn't set up face detector");
                    if(eyeSide == LEFT_SIDE) {
                        leftImage.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                    } else if(eyeSide == RIGHT_SIDE) {
                        rightImage.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                    }
                    Toast.makeText(getApplicationContext(),"Could not set up the face detector!", Toast.LENGTH_SHORT).show();
                    Snackbar.make(findViewById(android.R.id.content), "File saved at: "+ uri.getPath(), Snackbar.LENGTH_LONG).show();
                    return;
                }

                Frame frame = new Frame.Builder().setBitmap(img).build();
                SparseArray<Face> faces = faceDetector.detect(frame);
                if(faces.size() > 0) {
                    Toast toast = Toast.makeText(getApplicationContext(), String.valueOf(faces.size()) + " FACE DETECTED!!!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                for(int i=0; i<faces.size(); i++) {
                    Face thisFace = faces.valueAt(i);
                    float x1 = thisFace.getPosition().x;
                    float y1 = thisFace.getPosition().y;
                    float x2 = x1 + thisFace.getWidth();
                    float y2 = y1 + thisFace.getHeight();
                    tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myPaint);
                    Log.e("left prob=:",String.valueOf(thisFace.getIsLeftEyeOpenProbability()));
                    Log.e("right prob=:",String.valueOf(thisFace.getIsRightEyeOpenProbability()));
                    for(Landmark landmark : thisFace.getLandmarks()) {
                        if (landmark.getType() == Landmark.LEFT_EYE || landmark.getType() == Landmark.RIGHT_EYE) {
                            int cx = (int) landmark.getPosition().x;
                            int cy = (int) landmark.getPosition().y;
                            tempCanvas.drawCircle(cx, cy, 150, myPaint);
                        }
                    }
                }
                if(eyeSide == LEFT_SIDE) {
                    leftImage.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                } else if(eyeSide == RIGHT_SIDE) {
                    rightImage.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                }
                Snackbar.make(findViewById(android.R.id.content), "File saved at: "+ uri.getPath(), Snackbar.LENGTH_LONG).show();
            } catch (Exception e){
                e.printStackTrace();
            }

        } else {
            Log.e("ERROR", "can't get image");
            Log.e("resultCode=: ", String.valueOf(resultCode));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void showSensorToast(String message) {
        if(sensorToast != null) {
            sensorToast.cancel();
        }
        sensorToast = Toast.makeText(CameraActivity.this, message, Toast.LENGTH_SHORT);
        sensorToast.setGravity(Gravity.TOP, 0, 100);
        sensorToast.show();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if(abs(previousSensorValue - sensorEvent.values[0]) >= 5) {
            if(diagnoseMode == NORMAL_MODE) {
                if(sensorEvent.values[0] >= 50) {
                    showSensorToast("GOOD BRIGHTNESS");
                } else {
                    showSensorToast("TOO DARK");
                }
            } else if(diagnoseMode == RED_REFLECT_MODE) {
                if(sensorEvent.values[0] >= 50) {
                    showSensorToast("TOO BRIGHT");
                } else {
                    showSensorToast("GOOD DARKNESS");
                }
            } else {
                Log.e("ERROR", "Mode = -1, can't show sensor toast");
            }

            previousSensorValue = sensorEvent.values[0];
            lightBar.setProgress(Math.round(sensorEvent.values[0]));
            Log.e("Light sensor value=: ",String.valueOf(sensorEvent.values[0]));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}