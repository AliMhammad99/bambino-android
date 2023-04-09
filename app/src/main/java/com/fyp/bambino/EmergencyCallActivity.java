package com.fyp.bambino;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageButton;

import java.io.IOException;

public class EmergencyCallActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private ImageButton acceptCallButton;
    private ImageButton rejectCallButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_call);
        getSupportActionBar().hide();
        turnOnScreen();
        initUI();

        try {
            AssetFileDescriptor afd = getAssets().openFd("emergency_alarm.mp3");
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();
            player.setLooping(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


//        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.emergency_alarm);
//        mediaPlayer.setOnPreparedListener(this);
//        mediaPlayer.setLooping(true);
//        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                mediaPlayer.start();
//            }
//        });
//
//        mediaPlayer.prepareAsync();
    }

    private void initUI() {
        this.acceptCallButton = this.findViewById(R.id.btn_accept_call);
        this.rejectCallButton = this.findViewById(R.id.btn_reject_call);

    }

    private void turnOnScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
//    @Override
//    public void onPrepared(MediaPlayer mp) {
//        mediaPlayer.start();
//    }
//    @Override
//    protected void onStart() {
//        super.onStart();
////        try {
////            mediaPlayer.prepare();
////        } catch (IOException e) {
////            throw new RuntimeException(e);
////        }
//        mediaPlayer.start();
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        mediaPlayer.stop();
//        mediaPlayer.release();
//    }
}