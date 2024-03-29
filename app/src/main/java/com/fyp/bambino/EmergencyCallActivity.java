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
import android.widget.ImageView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class EmergencyCallActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private ImageButton acceptCallButton;
    private ImageButton rejectCallButton;
//    private Vibrator vibrator;

    private ImageView ivLiveVideo;
    private Timer liveVideoTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_call);
        getSupportActionBar().hide();
        turnOnScreen();
        initUI();
        startLiveVideo();
        try (AssetFileDescriptor afd = getAssets().openFd("emergency_alarm.mp3")) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
    }

    private void initUI() {
        this.acceptCallButton = this.findViewById(R.id.btn_accept_call);
        this.rejectCallButton = this.findViewById(R.id.btn_reject_call);
        this.ivLiveVideo = this.findViewById(R.id.live_video);
        this.acceptCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LiveVideoService.emergencyCallRunning = false;
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
                reEnableEmergencyCallAfterDelay();
            }
        });

        this.rejectCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LiveVideoService.emergencyCallRunning = false;
                // Get the Intent that started this activity
                finish();
                reEnableEmergencyCallAfterDelay();
            }

        });
    }

    private void turnOnScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void startLiveVideo() {
        this.liveVideoTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (LiveVideoService.currentFrameBitmap != null) {
                    ivLiveVideo.setImageBitmap(LiveVideoService.currentFrameBitmap);
                }
            }
        };
        this.liveVideoTimer.schedule(timerTask, 0, 200);
    }

    private void reEnableEmergencyCallAfterDelay() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                LiveVideoService.emergencyCallEnabled = true;
            }
        }, 10000);
    }

    @Override
    protected void onDestroy() {
        Log.i("Destroying Emergency Call", "!!!!!!!!!!!!!!!!!!!!!!!");
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        if (this.liveVideoTimer != null) {
            this.liveVideoTimer.cancel();
            this.liveVideoTimer = null;
        }


    }

}