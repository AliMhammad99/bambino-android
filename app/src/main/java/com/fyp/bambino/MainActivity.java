package com.fyp.bambino;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

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
//        editor.putString("mode", "0");
//        editor.apply();
        initUI();
        loadModeFromSharedPreferences();
        setupMode();
        String fragmentName = getIntent().getStringExtra("FRAGMENT_TO_LOAD");


        if (fragmentName!=null && fragmentName.equals("LIVE_VIDEO")) {
            Log.i("FRAGMENT NAME:  ",fragmentName);
            this.liveVideoButton.performClick();
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
                startLiveVideoLocalService();
                setupNavButton(this.liveVideoButton, new LiveVideoLocalFragment());
            } else {
                startLiveVideoRemoteService();
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

    private void startLiveVideoLocalService() {
        if (!liveVideoLocalServiceRunning()) {
            Intent serviceIntent = new Intent(this, LiveVideoLocalService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            }
        }
    }

    private void stopLiveVideoLocalService() {
        // Create an intent to stop the service
        Intent stopIntent = new Intent(this, LiveVideoLocalService.class);
        stopIntent.setAction("stop");
        // Start the service with the stop intent
        startService(stopIntent);
    }

    private void startLiveVideoRemoteService() {
        Log.i("REMOTE LIVE VIDEO SERVICE IS RUNNING", "");
    }

    private void stopLiveVideoRemoteService() {
        Log.i("REMOTE LIVE VIDEO SERVICE IS STOPPING", "");
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
            stopLiveVideoRemoteService();
            startLiveVideoLocalService();
            setupNavButton(findViewById(R.id.btn_live_video), new LiveVideoLocalFragment());
        } else {
            stopLiveVideoLocalService();
            startLiveVideoRemoteService();
            setupNavButton(findViewById(R.id.btn_live_video), new LiveVideoRemoteFragment());
        }
    }

    public boolean liveVideoLocalServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (LiveVideoLocalService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
