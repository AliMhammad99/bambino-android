package com.fyp.bambino;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();


        setupNavButton(findViewById(R.id.btn_dashboard));
        setupNavButton(findViewById(R.id.btn_live_video));
        setupNavButton(findViewById(R.id.btn_config));

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