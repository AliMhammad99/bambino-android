package com.fyp.bambino;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private ImageButton buttonDashBoard;
    private ImageButton buttonLiveVideo;
    private ImageButton buttonConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        buttonDashBoard = findViewById(R.id.btn_dashboard);
        buttonLiveVideo = findViewById(R.id.btn_live_video);
        buttonConfig = findViewById(R.id.btn_config);
        setupNavButton(buttonDashBoard);
        setupNavButton(buttonLiveVideo);
        setupNavButton(buttonConfig);

    }

    private void setupNavButton(ImageButton navButton){
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
            }
        });
    }
}