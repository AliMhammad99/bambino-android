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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashScreen extends AppCompatActivity {

    private ImageView imageView;
    private OkHttpClient client;
    private Request request;
    private String url;
    private String partBoundary = "123456789000000000000987654321";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        imageView = findViewById(R.id.imageView);
        // Get the SurfaceView component from the layout
        client = new OkHttpClient();
        url = "http://192.168.43.239/";
        request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "multipart/x-mixed-replace;boundary=" + partBoundary)
                .build();
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Parse the response and update the ImageView
                InputStream inputStream = response.body().byteStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("--" + partBoundary)) {
                        // Extract the image data and update the ImageView
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
            }
        };

// Start the HTTP request to receive the streamed images
        client.newCall(request).enqueue(callback);
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