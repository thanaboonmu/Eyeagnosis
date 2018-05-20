package com.example.sirapat.eyeagnosis;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.roger.catloadinglibrary.CatLoadingView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.abs;

public class CameraActivity extends AppCompatActivity implements SensorEventListener {

    private String IP;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 0;
    final private int NORMAL_MODE = 0;
    final private int RED_REFLECT_MODE = 1;
    final private int LEFT_SIDE = 0;
    final private int RIGHT_SIDE = 1;

    final private int CAMERA_INTENT = 1;
    final private int BROWSE_INTENT = 2;

    private Uri uri = null;
    private int diagnoseMode = -1;
    private String mode = "normal";
    private String usernameParam = "";
    private Sensor mLight = null;
    private SensorManager mSensorManager = null;
    private float previousSensorValue = 0.1f;
    private Toast sensorToast = null;
    private IconRoundCornerProgressBar lightBar = null;
    private ImageView leftImage = null;
    private ImageView rightImage = null;
    private int eyeSide = -1;
    private String imageFileName = null;
    private Bitmap leftBitmap = null;
    private Bitmap rightBitmap = null;
    private String leftFilePath = "";
    private String rightFilePath = "";
    private ProgressDialog progressDialog;
    private String leftResponse = "";
    private String rightResponse = "";
    private TextView modeText;

    File dir;

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
                    Intent i = new Intent(CameraActivity.this, SigninActivity.class);
                    CameraActivity.this.startActivity(i);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
            builder.setTitle("Confirmation").setMessage("Sign out?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Config config = new Config();
        IP = config.IP;

        setContentView(R.layout.activity_camera);
        setTitle("Normal test");
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            diagnoseMode = extras.getInt("mode");
            if(diagnoseMode == -1) {
                Log.e("ERROR","Mode = -1, something went wrong");
            }
            if (extras.getString("tempLeftRes") != null) {
                tempLeftRes = extras.getString("tempLeftRes");
            }
            if (extras.getString("tempRightRes") != null) {
                tempRightRes = extras.getString("tempRightRes");
            }
        } else {
            Log.e("ERROR", "Couldn't get mode");
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lightBar = (IconRoundCornerProgressBar) findViewById(R.id.lightProgressBar);
        modeText = (TextView) findViewById(R.id.modeTextView);
        String modeMsg = "NORMAL TEST\nต้อลม/ต้อเนื้อ";
        modeText.setText(modeMsg);
        modeText.setTextColor(Color.parseColor("#00A86B")); // JADE
        Switch switchMode = (Switch) findViewById(R.id.switch1);
        switchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (diagnoseMode == NORMAL_MODE) {
                    diagnoseMode = RED_REFLECT_MODE;
                    mode = "red_reflect";
                    setTitle("Red reflect test");
                    String modeMsg = "RED REFLECT TEST\nต้อกระจก/มะเร็งจอตา";
                    modeText.setText(modeMsg);
                    modeText.setTextColor(Color.parseColor("#FF0000")); // RED
                    lightBar.setIconImageResource(R.drawable.good_darkness);
                } else {
                    diagnoseMode = NORMAL_MODE;
                    mode = "normal";
                    setTitle("Normal test");
                    String modeMsg = "NORMAL TEST\nต้อลม/ต้อเนื้อ";
                    modeText.setText(modeMsg);
                    modeText.setTextColor(Color.parseColor("#00A86B")); // JADE
                    lightBar.setIconImageResource(R.drawable.brightness);
                }
            }
        });

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

        Button leftBrowseButton = (Button) findViewById(R.id.leftBrowseButton);
        leftBrowseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eyeSide = LEFT_SIDE;
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, BROWSE_INTENT);
            }
        });

        Button rightBrowseButton = (Button) findViewById(R.id.rightBrowseButton);
        rightBrowseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eyeSide = RIGHT_SIDE;
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, BROWSE_INTENT);
            }
        });

        SharedPreferences sp = getSharedPreferences("myjwt", Context.MODE_PRIVATE);
        final String token = sp.getString("token", "");
        final String username = sp.getString("username", "");
        if (token.equals("") && username.equals("")) {
            usernameParam = "";
        } else {
            usernameParam = "&username=" + username;
        }

        final CatLoadingView loading = new CatLoadingView();
        Button uploadButton = (Button) findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkConnected()) {
                    if (leftBitmap != null && rightBitmap != null) { // both sides
                        loading.show(getSupportFragmentManager(), "Analyzing");
                        Ion.with(CameraActivity.this)
                                .load("http://" + IP + ":8080/api/image?side=left&mode=" + mode + usernameParam)
                                .setTimeout(300000)
                                .setHeader("Authorization", "Bearer " + token)
                                .progressDialog(progressDialog)
                                .setMultipartParameter("name", "source")
                                .setMultipartFile("image", "image/png", new File(leftFilePath))
                                .asJsonObject()
                                .setCallback(new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, JsonObject result) {
                                        if (result != null) {
                                            leftResponse = result.toString();
                                            Ion.with(CameraActivity.this)
                                                    .load("http://" + IP + ":8080/api/image?side=right&mode=" + mode + usernameParam)
                                                    .setTimeout(300000)
                                                    .setHeader("Authorization", "Bearer " + token)
                                                    .progressDialog(progressDialog)
                                                    .setMultipartParameter("name", "source")
                                                    .setMultipartFile("image", "image/png", new File(rightFilePath))
                                                    .asJsonObject()
                                                    .setCallback(new FutureCallback<JsonObject>() {
                                                        @Override
                                                        public void onCompleted(Exception e, JsonObject result) {
                                                            loading.dismiss();
                                                            if (result != null) {
                                                                rightResponse = result.toString();
                                                                Intent i = new Intent(CameraActivity.this, Result.class);
                                                                i.putExtra("leftResponse", leftResponse);
                                                                i.putExtra("rightResponse", rightResponse);
                                                                CameraActivity.this.startActivity(i);
                                                            } else {
                                                                Toast.makeText(CameraActivity.this, "Server isn't running", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            loading.dismiss();
                                            Toast.makeText(CameraActivity.this, "Server isn't running", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else if (leftBitmap != null || rightBitmap != null) {
                        if (leftBitmap != null) { // left side
                            loading.show(getSupportFragmentManager(), "Analyzing");
                            Ion.with(CameraActivity.this)
                                    .load("http://" + IP + ":8080/api/image?side=left&mode=" + mode + usernameParam)
                                    .setTimeout(200000)
                                    .setHeader("Authorization", "Bearer " + token)
                                    .progressDialog(progressDialog)
                                    .setMultipartParameter("name", "source")
                                    .setMultipartFile("image", "image/png", new File(leftFilePath))
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, JsonObject result) {
                                            loading.dismiss();
                                            if(result != null) {
                                                leftResponse = result.toString();
                                                Intent i = new Intent(CameraActivity.this, Result.class);
                                                i.putExtra("leftResponse", leftResponse);
                                                CameraActivity.this.startActivity(i);
                                            } else {
                                                Toast.makeText(CameraActivity.this, "Server isn't running", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else { //right side
                            loading.show(getSupportFragmentManager(), "Analyzing");
                            Ion.with(CameraActivity.this)
                                    .load("http://" + IP + ":8080/api/image?side=right&mode=" + mode + usernameParam)
                                    .setTimeout(200000)
                                    .setHeader("Authorization", "Bearer " + token)
                                    .progressDialog(progressDialog)
                                    .setMultipartParameter("name", "source")
                                    .setMultipartFile("image", "image/png", new File(rightFilePath))
                                    .asJsonObject()
                                    .setCallback(new FutureCallback<JsonObject>() {
                                        @Override
                                        public void onCompleted(Exception e, JsonObject result) {
                                            loading.dismiss();
                                            if (result != null) {
                                                rightResponse = result.toString();
                                                Intent i = new Intent(CameraActivity.this, Result.class);
                                                i.putExtra("rightResponse", rightResponse);
                                                CameraActivity.this.startActivity(i);
                                            } else {
                                                Toast.makeText(CameraActivity.this, "Server isn't running", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(CameraActivity.this, "At least 1 eye is required", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CameraActivity.this, "Can't upload, no internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // For Android 7+ (Nougat) File system
        if(Build.VERSION.SDK_INT >= 24) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
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
        imageFileName = "IMG_" + timestamp + ".png";
        dir = new File(Environment.getExternalStorageDirectory(), "DCIM/eyeagnosis");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        dir = new File(dir, "/" + imageFileName);
        uri = Uri.fromFile(dir);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CAMERA_INTENT);
    }

    private Bitmap rotateImage(Bitmap originalBitmap, int angle) {
        Matrix mat = new Matrix();
        mat.postRotate((angle));
        return Bitmap.createBitmap(originalBitmap, 0,0, originalBitmap.getWidth(), originalBitmap.getHeight(), mat, true);
    }

    private Bitmap checkOrientation(Bitmap img, String filepath) {
        try {
            ExifInterface ei = new ExifInterface(filepath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(img, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(img, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(img, 270);
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    return img;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return img;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_INTENT && resultCode == RESULT_OK) {
            try {
                // put the saved image into gallery
                Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(uri);
                sendBroadcast(mediaScanIntent);
                CropImage.activity(uri)
                        .setAspectRatio(16,9)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .start(CameraActivity.this);
            } catch (Exception e){
                Log.e("error", e.toString());
            }

        } else if(requestCode == BROWSE_INTENT && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            dir = new File(picturePath);
            uri = Uri.fromFile(dir);
            CropImage.activity(uri)
                    .setAspectRatio(16,9)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .start(CameraActivity.this);
//            if (eyeSide == LEFT_SIDE) {
//                leftBitmap = BitmapFactory.decodeFile(picturePath);
//                leftBitmap = checkOrientation(leftBitmap, picturePath);
//                leftImage.setImageBitmap(leftBitmap);
//                leftFilePath = picturePath;
//            } else if (eyeSide == RIGHT_SIDE){
//                rightBitmap = BitmapFactory.decodeFile(picturePath);
//                rightBitmap = checkOrientation(rightBitmap, picturePath);
//                rightImage.setImageBitmap(rightBitmap);
//                rightFilePath = picturePath;
//            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                getContentResolver().notifyChange(resultUri, null);
                ContentResolver cr = getContentResolver();
                Bitmap img = MediaStore.Images.Media.getBitmap(cr,resultUri);
//                img = rotateImage(img, -90);
//                img = checkOrientation(img, "/storage/emulated/0/DCIM/eyeagnosis/" + imageFileName);
                FileOutputStream fos = new FileOutputStream(dir);
                img.compress(Bitmap.CompressFormat.PNG, 100, fos);
                uri = Uri.fromFile(dir);
                if(eyeSide == LEFT_SIDE) {
                    leftBitmap = img;
                    leftFilePath = uri.getPath();
                    leftImage.setImageBitmap(img);
                } else if (eyeSide == RIGHT_SIDE) {
                    rightBitmap = img;
                    rightFilePath = uri.getPath();
                    rightImage.setImageBitmap(img);
                }
                Snackbar.make(findViewById(android.R.id.content), "File saved at: "+ uri.getPath(), Snackbar.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("error", e.toString());
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
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
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

        if(abs(previousSensorValue - sensorEvent.values[0]) >= 5 || previousSensorValue == 0.1f) {
            if(diagnoseMode == NORMAL_MODE) {
                if(sensorEvent.values[0] >= 50) {
                    lightBar.setIconImageResource(R.drawable.brightness);
                    showSensorToast("GOOD BRIGHTNESS");
                } else {
                    lightBar.setIconImageResource(R.drawable.too_dark);
                    showSensorToast("TOO DARK");
                }
            } else if(diagnoseMode == RED_REFLECT_MODE) {
                if(sensorEvent.values[0] >= 10) {
                    lightBar.setIconImageResource(R.drawable.too_bright);
                    showSensorToast("TOO BRIGHT");
                } else {
                    lightBar.setIconImageResource(R.drawable.good_darkness);
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