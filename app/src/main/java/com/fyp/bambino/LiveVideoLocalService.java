package com.fyp.bambino;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

public class LiveVideoLocalService extends Service {

    int counter;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        counter = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Log.i("Service Running ", String.valueOf(counter));
                    counter++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
        final String CHANNEL_ID = "Local Live Video";
        NotificationChannel notificationChannel = null;
        // Create a PendingIntent for when the notification is clicked
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_live_video_foreground_service);
        // Set the maximum height of the custom view to match the notification height
        int notificationHeight = getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
        remoteViews.setViewPadding(R.id.notification_layout, 0, 0, 0, notificationHeight);
        AudioAttributes attributes = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setSound(null, null);

            getSystemService(NotificationManager.class).createNotificationChannel(notificationChannel);
            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_bambino)
                    .setCustomContentView(remoteViews)
                    .setContentIntent(pendingIntent);

            startForeground(1001, notification.build());
        }

        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
