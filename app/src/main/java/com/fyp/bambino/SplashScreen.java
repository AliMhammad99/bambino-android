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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
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
    private String getFlashLEDUrl = "https://bambinoserver0.000webhostapp.com/get_flash_led.php";
    private String setFlashLEDUrl = "https://bambinoserver0.000webhostapp.com/set_flash_led.php?flashLED=";

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

        Switch flashLEDButton = findViewById(R.id.flash_led_toggle_button);
        getFlashLEDFromServer(flashLEDButton);


        flashLEDButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Code to be executed when the switch button is checked or unchecked
                setFlashLEDOnServer(isChecked);
            }
        });
    }

    private void getFlashLEDFromServer(Switch flashLEDButton) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getFlashLEDUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        boolean result = Boolean.parseBoolean(response);
                        flashLEDButton.setChecked(result);
                        // Handle the boolean value
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
            }
        });
        requestQueue.add(stringRequest);

    }

    private void setFlashLEDOnServer(boolean flashLED) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, setFlashLEDUrl + flashLED, null, null);
        requestQueue.add(stringRequest);
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

}