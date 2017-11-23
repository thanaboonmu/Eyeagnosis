package com.example.sirapat.eyeagnosis;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.File;
import java.util.Date;

public class CameraActivity2 extends AppCompatActivity implements SensorEventListener {

    private ImageView imageView;
    private Uri uri;
    private int tag = 1;
    private Sensor mLight;
    private SensorManager mSensorManager;
    private Button imageButton = null;
    private ProgressBar bar = null;
    private IconRoundCornerProgressBar bar2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        imageButton = (Button) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera(view);
            }
        });
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        imageView = (ImageView) findViewById(R.id.imageView);
//        bar = (ProgressBar)findViewById(R.id.progressBar);
        bar2 = (IconRoundCornerProgressBar) findViewById(R.id.progress_2);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    public void openCamera(View view) {

        checkUserPermissions();
    }

    void checkUserPermissions(){
        if ( Build.VERSION.SDK_INT >= 23){
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
    //get access to location permission
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    // Permission Denied
                    Toast.makeText( this,"Can't save image" , Toast.LENGTH_SHORT)
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
//        File f = new File(getFilesDir(), "/eyeagnosis/" + imageFileName);
//        File f = new File(Environment.getDataDirectory(), "/eyeagnosis/" + imageFileName);
        File f = new File(Environment.getExternalStorageDirectory(), "DCIM/Camera/" + imageFileName);
        uri = Uri.fromFile(f);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, tag);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == tag && resultCode == RESULT_OK) {
            getContentResolver().notifyChange(uri, null);
            ContentResolver cr = getContentResolver();
            try {
                Bitmap img = MediaStore.Images.Media.getBitmap(cr, uri);

//                Matrix mat = new Matrix(); //
//                mat.postRotate((-90)); // only for sss8 (temp solution)
//                img = Bitmap.createBitmap(img, 0,0, img.getWidth(), img.getHeight(), mat, true); //

                Paint myPaint = new Paint();
                myPaint.setColor(Color.YELLOW);
                myPaint.setStyle(Paint.Style.STROKE);
                myPaint.setStrokeWidth(3);

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
                    imageView.setImageBitmap(img);
                    Toast.makeText(getApplicationContext(),"Could not set up the face detector!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Frame frame = new Frame.Builder().setBitmap(img).build();
                SparseArray<Face> faces = faceDetector.detect(frame);
                if(faces.size() > 0) {
                    Toast toast = Toast.makeText(getApplicationContext(), String.valueOf(faces.size()) + " FACE DETECTED!!!", Toast.LENGTH_LONG);
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

                imageView.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));

//                imageView.setImageBitmap(img);
                Toast.makeText(getApplicationContext(), "File saved at: "+ uri.getPath(), Toast.LENGTH_SHORT).show();

            } catch (Exception e){
                e.printStackTrace();
            }
//            Bundle b = data.getExtras();
//            Bitmap img = (Bitmap) b.get("data");
//            imageView.setImageBitmap(img);
        } else {
            Log.e("ERROR", "can't get image");
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

    public void showSensorToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        bar.setProgress(Math.round(sensorEvent.values[0]));
        bar2.setProgress(Math.round(sensorEvent.values[0]));
        if(sensorEvent.values[0] >= 50) {
            showSensorToast("GOOD BRIGHTNESS");
        } else if (sensorEvent.values[0] <= 50){
            showSensorToast("TOO DARK");
        } else {
            showSensorToast("TOO BRIGHT");
        }
        Log.e("Light sensor value=: ",String.valueOf(sensorEvent.values[0]));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}