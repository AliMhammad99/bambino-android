package com.fyp.bambino;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import java.io.IOException;

public class EmergencyCallActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private ImageButton acceptCallButton;
    private ImageButton rejectCallButton;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_call);
        getSupportActionBar().hide();
        turnOnScreen();
        initUI();

        try {
            AssetFileDescriptor afd = getAssets().openFd("emergency_alarm.mp3");
            this.mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            long[] pattern = {0, 1000}; // Vibrate for 1 second, then pause for 1 second
            vibrator.vibrate(pattern, 0); // Start the vibration pattern
        }

    }

    private void initUI() {
        this.acceptCallButton = this.findViewById(R.id.btn_accept_call);
        this.rejectCallButton = this.findViewById(R.id.btn_reject_call);

        this.acceptCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Unlock the screen
                KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    keyguardManager.requestDismissKeyguard(EmergencyCallActivity.this, null);
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        // Start the MainActivity with extra specifying which fragment to use
                        Intent intent = new Intent(EmergencyCallActivity.this, MainActivity.class);
                        intent.putExtra("FRAGMENT_TO_LOAD", "LIVE_VIDEO");
                        startActivity(intent);
                        finish();
                    }
                }, 0);
            }
        });

        this.rejectCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void turnOnScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        if (vibrator != null) {
            vibrator.cancel(); // Stop the vibration pattern
        }
    }
}