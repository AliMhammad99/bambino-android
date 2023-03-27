package com.fyp.bambino;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashScreen extends AppCompatActivity {

    private ImageView imageView;

    private boolean flashOn = false;

    private boolean updatingFlashState = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        imageView = findViewById(R.id.imageView);



        StreamThread streamThread = new StreamThread();
        Thread thread = new Thread(streamThread);
        thread.start();

        FlashLEDThread flashLEDThread = new FlashLEDThread();
        Thread thread1 = new Thread(flashLEDThread);
        thread1.start();

        Switch flashLEDSwitch = findViewById(R.id.flash_led_switch);
        flashLEDSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updatingFlashState = true;
            }
        });

    }


    private void Bytes2ImageFile(byte[] bytes, String fileName) {
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes, 0, bytes.length);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class StreamThread implements Runnable {

        @Override
        public void run() {
            while(true){
                if(!updatingFlashState){
                    String stream_url = "http://192.168.43.239:80";

                    BufferedInputStream bis = null;
                    FileOutputStream fos = null;
                    try {

                        URL url = new URL(stream_url);

                        try {
                            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                            huc.setRequestMethod("GET");
                            huc.setConnectTimeout(1000 * 5);
                            huc.setReadTimeout(1000 * 5);
                            huc.setDoInput(true);
                            huc.connect();

                            if (huc.getResponseCode() == 200) {
                                InputStream in = huc.getInputStream();

                                InputStreamReader isr = new InputStreamReader(in);
                                BufferedReader br = new BufferedReader(isr);

                                String data;

                                int len;
                                byte[] buffer;

                                while ((data = br.readLine()) != null&&!updatingFlashState) {
                                    Log.i("DATA:   ",data);
                                    if (data.contains("Content-Type:")) {
                                        data = br.readLine();

                                        len = Integer.parseInt(data.split(":")[1].trim());

                                        bis = new BufferedInputStream(in);
                                        buffer = new byte[len];

                                        int t = 0;
                                        while (t < len) {
                                            t += bis.read(buffer, t, len - t);
                                        }

                                        Bytes2ImageFile(buffer, getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");

                                        final Bitmap responseBitmap = BitmapFactory.decodeFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");
                                        Matrix matrix = new Matrix();

                                        matrix.postRotate(90);
                                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(responseBitmap, responseBitmap.getWidth(), responseBitmap.getHeight(), true);

                                        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                imageView.setImageBitmap(rotatedBitmap);
                                            }
                                        });

                                    }


                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (bis != null) {
                                bis.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private class FlashLEDThread implements Runnable{

        @Override
        public void run() {
            while (true){
                if(updatingFlashState){
                    flashOn ^= true;

                    String flash_url;
                    if (flashOn) {
                        flash_url = "http://192.168.43.239:80/flash_on";
                    } else {
                        flash_url = "http://192.168.43.239:80/flash_off";
                    }

                    try {

                        URL url = new URL(flash_url);

                        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                        huc.setRequestMethod("GET");
                        huc.setConnectTimeout(1000 * 5);
                        huc.setReadTimeout(1000 * 5);
                        huc.setDoInput(true);
                        huc.connect();
                        if (huc.getResponseCode() == 200) {
                            InputStream in = huc.getInputStream();

                            InputStreamReader isr = new InputStreamReader(in);
                            BufferedReader br = new BufferedReader(isr);

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updatingFlashState = false;
                }
            }
        }
    }
}