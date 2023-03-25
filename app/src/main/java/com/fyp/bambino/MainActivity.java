package com.fyp.bambino;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private ImageButton currentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();



        setupNavigation();

    }

    private void setupNavigation(){
        currentButton = findViewById(R.id.btn_dashboard);
        currentButton.setSelected(true);
        setupNavButton(findViewById(R.id.btn_dashboard), new DashBoardFragment());
        setupNavButton(findViewById(R.id.btn_live_video), new LiveVideoFragment());
        setupNavButton(findViewById(R.id.btn_config), new ConfigFragment());
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
}