package com.example.sirapat.eyeagnosis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;

/**
 * Created by sirapat on 1/25/2018 AD.
 */

public class HttpUpload extends AsyncTask<Void, Integer, Void> {

    private Bitmap imageToSend = null;
    private Context context;
    private ProgressDialog progressDialog;
    private String response = "";

    public HttpUpload(Context context, Bitmap imageToSend) {
        this.context = context;
        this.imageToSend = imageToSend;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog = ProgressDialog.show(context, "Upload to server", "Uploading...", true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
//            final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            imageToSend.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            byte[] bitmapData = stream.toByteArray();
//
//            OkHttpClient client = new OkHttpClient();
//            RequestBody requestBody = new MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addPart(RequestBody.create(MEDIA_TYPE_PNG, bitmapData))
//                    .build();
//            final Request request = new Request.Builder()
//                    .url("http://192.168.1.100:8080/upload-image")
//                    .post(requestBody)
//                    .build();
//            Response response = client.newCall(request).execute();
//            Log.e("response", response.toString());
            URL url = new URL("http://192.168.1.100:8080/upload-image");
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
            try {
                JSONObject reader = new JSONObject(response);
                response = reader.getString("status");
            } catch (JSONException e) {
                Log.e("","unexpected JSON exception", e);
            }

            conn.disconnect();

        } catch(MalformedURLException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
//        progressDialog.setProgress((int) (progress[0]));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();

        Intent i = new Intent(context, Result.class);
        i.putExtra("response", response);
        context.startActivity(i);
    }
}
