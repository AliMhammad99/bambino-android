package com.fyp.bambino;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.FragmentTransaction;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

public class MainActivity extends AppCompatActivity {

    private ImageButton currentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        setupNavigation();

        LinearLayout navigation = findViewById(R.id.navigation);

        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if(isOpen){
                    navigation.setVisibility(View.GONE);
                }else{
                    navigation.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void setupNavigation(){
        currentButton = findViewById(R.id.btn_dashboard);
        currentButton.setSelected(true);
        setupNavButton(findViewById(R.id.btn_dashboard), new DashBoardFragment());
        setupNavButton(findViewById(R.id.btn_live_video), new LiveVideoFragment());
        setupNavButton(findViewById(R.id.btn_config), new ConfigFragmentStep2());
    }

    private void setupNavButton(ImageButton navButton, Fragment fragment){
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

    public void goToConfigFragmentStep2(){
        // Create an instance of the second fragment.
        ConfigFragmentStep2 configFragmentStep2 = new ConfigFragmentStep2();

//        // Pass the BluetoothSocket instance to the second fragment as an argument.
//        Bundle args = new Bundle();
//        args.putParcelable("bluetoothSocket", socket);
//        configFragmentStep2.setArguments(args);

        // Replace the current fragment with the second fragment.
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, configFragmentStep2)
                .commit();
    }
}