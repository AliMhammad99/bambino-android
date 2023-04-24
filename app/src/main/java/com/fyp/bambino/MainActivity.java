package com.fyp.bambino;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private ImageButton dashboardButton;
    private ImageButton liveVideoButton;
    private ImageButton configButton;
    private ImageButton currentButton;

    private String mode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
//        SharedPreferences sharedPreferences = getSharedPreferences("bambino", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("mode", "1");
//        editor.apply();
        initUI();
        loadModeFromSharedPreferences();
        setupMode();
        String fragmentName = getIntent().getStringExtra("FRAGMENT_TO_LOAD");


        if (fragmentName != null && fragmentName.equals("LIVE_VIDEO")) {
            Log.i("FRAGMENT NAME:  ", fragmentName);
            this.liveVideoButton.performClick();
        }

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
//        String url = "http://3666-34-86-115-151.ngrok-free.app/upload";

//        FlaskThread flaskThread = new FlaskThread();
//        flaskThread.start();
    }

    private class FlaskThread extends Thread {
        @Override
        public void run() {
            Log.i("FlaskThread:    ", "RUNNING");
            // Perform network operations here
            // Load the Bitmap from a drawable resource
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            URL url = null;
            try {
                url = new URL("https://e5a8-35-204-88-29.ngrok-free.app/upload");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("image", encodedImage);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(jsonRequest.toString().getBytes());
                outputStream.flush();
                outputStream.close();

                int statusCode = connection.getResponseCode();
                Log.i("RESPONSE CODE:   ", String.valueOf(statusCode));
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    String response = "";
                    while ((line = in.readLine()) != null) {
                        response += line;
                    }
                    Log.i("RESPONSE:    ", response);
                    in.close();
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void initUI() {
        this.dashboardButton = this.findViewById(R.id.btn_dashboard);
        this.liveVideoButton = this.findViewById(R.id.btn_live_video);
        this.configButton = this.findViewById(R.id.btn_config);

        LinearLayout navigation = findViewById(R.id.navigation);

        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (isOpen) {
                    navigation.setVisibility(View.GONE);
                } else {
                    navigation.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadModeFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("bambino", Context.MODE_PRIVATE);

        this.mode = sharedPreferences.getString("mode", "");
        Log.i("MODE IN MAIN: ", this.mode);

    }

    private void setupMode() {
        currentButton = this.dashboardButton;
        currentButton.setSelected(true);

        if (noConnectedDevice()) {
            setupNavButton(this.dashboardButton, new DashBoardNoCDFragment());
            setupNavButton(this.liveVideoButton, new LiveVideoNoCDFragment());
        } else {
            if (this.isLocalMode()) {
                startLiveVideoService("local");
                setupNavButton(this.liveVideoButton, new LiveVideoLocalFragment());
            } else {
                startLiveVideoService("remote");
                setupNavButton(this.liveVideoButton, new LiveVideoRemoteFragment());
            }
            DashBoardFragment dashBoardFragment = new DashBoardFragment();
            setCurrentFragment(dashBoardFragment);
            setupNavButton(this.dashboardButton, dashBoardFragment);

        }
        setupNavButton(this.configButton, new ConfigFragmentStep1());
    }

    private void setupNavButton(ImageButton navButton, Fragment fragment) {
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentButton.setSelected(false);

                view.setSelected(true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .commit();
                currentButton = (ImageButton) view;
            }
        });
    }

    private void startLiveVideoService(String action) {
        if (!liveVideoLocalServiceRunning()) {
            Intent serviceIntent = new Intent(this, LiveVideoService.class);
            serviceIntent.setAction(action);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            }
        }
    }

    private void stopLiveVideoService() {
        // Create an intent to stop the service
        Intent stopIntent = new Intent(this, LiveVideoService.class);
        stopIntent.setAction("stop");
        // Start the service with the stop intent
        startService(stopIntent);
    }


    public void goToConfigFragmentStep2() {
        // Create an instance of the second fragment.
        ConfigFragmentStep2 configFragmentStep2 = new ConfigFragmentStep2();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, configFragmentStep2)
                .commit();
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .commit();
    }


    private boolean noConnectedDevice() {
        return this.mode.equals("");
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    private boolean isLocalMode() {
        return this.mode.equals("0");
    }

    public void updateMode() {
        setupNavButton(findViewById(R.id.btn_dashboard), new DashBoardFragment());
        if (this.isLocalMode()) {
            stopLiveVideoService();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Code to be executed after delay
                    startLiveVideoService("local");
                }
            }, 3000);

            setupNavButton(findViewById(R.id.btn_live_video), new LiveVideoLocalFragment());
        } else {
            stopLiveVideoService();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Code to be executed after delay
                    startLiveVideoService("remote");
                }
            }, 3000);

            setupNavButton(findViewById(R.id.btn_live_video), new LiveVideoRemoteFragment());
        }
    }

    public boolean liveVideoLocalServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (LiveVideoService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
