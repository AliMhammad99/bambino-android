package com.fyp.bambino;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

public class MainActivity extends AppCompatActivity {

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

        if (!liveVideoLocalServiceRunning()) {
            Intent serviceIntent = new Intent(this, LiveVideoLocalService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            }
        }


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

        loadModeFromSharedPreferences();
        setupNavigation();
    }

    private void setupNavigation() {
        currentButton = findViewById(R.id.btn_dashboard);
        currentButton.setSelected(true);

        if (noConnectedDevice()) {
            setupNavButton(findViewById(R.id.btn_dashboard), new DashBoardNoCDFragment());
            setupNavButton(findViewById(R.id.btn_live_video), new LiveVideoNoCDFragment());
        } else {
            DashBoardFragment dashBoardFragment = new DashBoardFragment();
            setCurrentFragment(dashBoardFragment);
            setupNavButton(findViewById(R.id.btn_dashboard), dashBoardFragment);
            if (this.isLocalMode()) {
                setupNavButton(findViewById(R.id.btn_live_video), new LiveVideoLocalFragment());
            } else {
                setupNavButton(findViewById(R.id.btn_live_video), new LiveVideoRemoteFragment());
            }
        }
        setupNavButton(findViewById(R.id.btn_config), new ConfigFragmentStep1());
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

    public void goToConfigFragmentStep2() {
        // Create an instance of the second fragment.
        ConfigFragmentStep2 configFragmentStep2 = new ConfigFragmentStep2();

//        // Pass the BluetoothSocket instance to the second fragment as an argument.
//        Bundle args = new Bundle();
//        args.putParcelable("bluetoothSocket", socket);
//        configFragmentStep2.setArguments(args);

        // Replace the current fragment with the second fragment.
//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, configFragmentStep2)
                .commit();
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .commit();
    }

    private void loadModeFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("bambino", Context.MODE_PRIVATE);
        this.mode = sharedPreferences.getString("mode", "");

    }

    private boolean noConnectedDevice() {
        return this.mode.equals("");
    }

//    public static String getMode() {
//        return MainActivity.mode;
//    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    private boolean isLocalMode() {
        return this.mode.equals("0");
    }

    public void updateNavigation() {
        setupNavButton(findViewById(R.id.btn_dashboard), new DashBoardFragment());
        if (this.isLocalMode()) {
            setupNavButton(findViewById(R.id.btn_live_video), new LiveVideoLocalFragment());
        } else {
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
