package com.fyp.bambino;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashScreen extends AppCompatActivity {

    private ImageView imageView;
//    private SurfaceView surfaceView;
    private Handler handler;
    private boolean streaming = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        // Get the SurfaceView component from the layout
//        surfaceView = findViewById(R.id.surfaceView);
        imageView = findViewById(R.id.imageView);
//        WebView webView = (WebView) findViewById(R.id.webview);
////        myWebView.loadUrl("http://192.168.43.239/");
//        webView.setWebViewClient(new WebViewClient()); // ensure links open in the same WebView
//        webView.loadUrl("http://192.168.43.239:80");
//        webView.getSettings().setJavaScriptEnabled(true); // enable JavaScript (required for some video players)
        startStream();
    }

    private void startStream() {
        // Start a new thread for the camera stream
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Set up the HTTP connection to the ESP32-CAM camera web server
                    URL url = new URL("http://192.168.43.239");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.connect();
                    Log.i("Connected","....");
                    // Get the input stream for the camera stream
                    InputStream stream = connection.getInputStream();

                    // Continuously read and display the camera stream frames
                    while (streaming) {
                        Log.i("Streaming:","....");
                        Log.i("STREAM:   ", String.valueOf(stream));
                        // Decode the next frame from the stream
                        Bitmap frame = BitmapFactory.decodeStream(stream);
                        imageView.setImageBitmap(frame);
                        // Update the SurfaceView with the new frame
//                        handler.post(new UpdateSurfaceView(frame));
                    }

                    // Clean up the connection and input stream
                    stream.close();
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void stopStream() {
        // Stop the camera stream
        streaming = false;
    }

//    private class UpdateSurfaceView implements Runnable {
//        private Bitmap frame;
//
//        public UpdateSurfaceView(Bitmap frame) {
//            this.frame = frame;
//        }
//
//        @Override
//        public void run() {
//            // Get the Canvas object from the SurfaceView's SurfaceHolder
//            Canvas canvas = surfaceView.getHolder().lockCanvas();
//
//            if (canvas != null) {
//                try {
//                    // Draw the new frame onto the Canvas
//                    canvas.drawBitmap(frame, 0, 0, null);
//                } finally {
//                    // Unlock the Canvas and update the SurfaceView
//                    surfaceView.getHolder().unlockCanvasAndPost(canvas);
//                }
//            }
//        }
//    }

}