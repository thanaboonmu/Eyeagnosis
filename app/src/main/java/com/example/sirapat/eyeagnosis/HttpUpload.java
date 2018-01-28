package com.example.sirapat.eyeagnosis;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by sirapat on 1/25/2018 AD.
 */

public class HttpUpload extends AsyncTask<Integer, Integer, String> {

    final private int LEFT_SIDE = 0;
    final private int RIGHT_SIDE = 1;
    private Bitmap imageToSend = null;
    private Context context;

    public HttpUpload(Context context, Bitmap imageToSend) {
        this.context = context;
        this.imageToSend = imageToSend;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Integer... side) {
        try {
            String response = "";
            String param = "";
            if (side[0] == LEFT_SIDE) {
                param = "left";
            } else if (side[0] == RIGHT_SIDE) {
                param = "right";
            }
            URL url = new URL("http://192.168.1.100:8080/upload-image?side=" + param);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setReadTimeout(35000);
            conn.setConnectTimeout(35000);

            OutputStream os = conn.getOutputStream();
            imageToSend.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();

            Log.e("","Response Code: " + conn.getResponseCode());
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = responseStreamReader.readLine()) != null)
                stringBuilder.append(line).append("\n");
            responseStreamReader.close();

            response = stringBuilder.toString();
            conn.disconnect();

            return response;

        } catch(MalformedURLException e) {
            e.printStackTrace();
            return String.valueOf(e);
        }
        catch(IOException e) {
            e.printStackTrace();
            return String.valueOf(e);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
//        progressDialog.setProgress((int) (progress[0]));
    }

//    @Override
//    protected void onPostExecute(String res) {
//        super.onPostExecute(res);
//        progressDialog.dismiss();

//    }

}
