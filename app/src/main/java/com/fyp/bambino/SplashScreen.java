package com.fyp.bambino;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
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
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    private ImageView imageView;
    private RequestQueue requestQueue;
    private String imageUrl = "https://bambinoserver0.000webhostapp.com/image.jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        imageView = findViewById(R.id.live_video);
        requestQueue = Volley.newRequestQueue(this);

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Call your PHP server here
                        getImageFromServer();
                    }
                });
            }
        };
        // Schedule the timer to run every 1 second
        timer.schedule(timerTask, 0, 2000);
    }

    private void getImageFromServer() {
        ImageRequest imageRequest = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap responseBitmap) {
                        Matrix matrix = new Matrix();

                        matrix.postRotate(90);

                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(responseBitmap, responseBitmap.getWidth(), responseBitmap.getHeight(), true);

                        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                        imageView.setImageBitmap(rotatedBitmap);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SplashScreen.this, "Error retrieving image", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(imageRequest);
    }

    private void renderToImageView(DataSnapshot dataSnapshot) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (long i = 0; i < (long) dataSnapshot.child("nbSubStrings").getValue(); i++) {
            String base64Chunk = (String) dataSnapshot.child("c").child("p" + i).getValue();
//            Log.i("DECODING: ", String.valueOf(i));
//            Log.i("DECODING: ", (String) dataSnapshot.child("c").child("p" + i).getValue());
            byte[] decodedBytes = Base64.decode(base64Chunk, Base64.DEFAULT);
            try {
                outputStream.write(decodedBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String base64Chunk = (String) dataSnapshot.child("c").child("pL").getValue();
        byte[] decodedBytes = Base64.decode(base64Chunk, Base64.DEFAULT);
        try {
            outputStream.write(decodedBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte fullDecodedBytes[] = outputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(fullDecodedBytes, 0, fullDecodedBytes.length);

        ImageView imageView = findViewById(R.id.live_video);

        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        imageView.setImageBitmap(rotatedBitmap);
    }
}