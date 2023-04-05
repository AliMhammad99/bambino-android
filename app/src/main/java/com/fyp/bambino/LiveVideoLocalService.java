package com.fyp.bambino;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioAttributes;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LiveVideoLocalService extends Service {


    int counter;
    RemoteViews customForegroundNotificationView;
    Notification.Builder foregroundNotification;

    public static Bitmap currentFrameBitmap;
    public static boolean flashUpdating = false;
    private String localURL = "http://192.168.0.107:80";

    @Override
    public void onCreate() {
        super.onCreate();
        final String CHANNEL_ID = "Bambino Local Live Video";
        NotificationChannel notificationChannel = null;
        // Create a PendingIntent for when the notification is clicked
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.customForegroundNotificationView = new RemoteViews(getPackageName(), R.layout.layout_live_video_foreground_service);

        // Set the maximum height of the custom view to match the notification height
        int notificationHeight = getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
        this.customForegroundNotificationView.setViewPadding(R.id.notification_layout, 0, 0, 0, notificationHeight);
        AudioAttributes attributes = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.setSound(null, null);

            getSystemService(NotificationManager.class).createNotificationChannel(notificationChannel);
            this.foregroundNotification = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_bambino)
                    .setCustomContentView(this.customForegroundNotificationView)
                    .setContentIntent(pendingIntent);

            startForeground(1001, this.foregroundNotification.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        counter = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!flashUpdating) {
//                    Log.i("Service Running ", String.valueOf(counter));
                        counter++;
                        customForegroundNotificationView.setTextViewText(R.id.data, counter + "");
                        foregroundNotification.setCustomContentView(customForegroundNotificationView);
                        startForeground(1001, foregroundNotification.build());
                        BufferedInputStream bis = null;
                        FileOutputStream fos = null;
                        try {

                            URL url = new URL(localURL);

                            try {
                                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                                huc.setRequestMethod("GET");
                                huc.setConnectTimeout(1000 * 5);
                                huc.setReadTimeout(1000 * 5);
                                huc.setDoInput(true);
                                huc.connect();

                                if (huc.getResponseCode() == 200) {

                                    InputStream in = huc.getInputStream();

                                    InputStreamReader isr = new InputStreamReader(in);
                                    BufferedReader br = new BufferedReader(isr);

                                    String data;

                                    int len;
                                    byte[] buffer;

                                    while ((data = br.readLine()) != null && !flashUpdating) {
                                        if (data.contains("Content-Type:")) {
                                            data = br.readLine();

                                            len = Integer.parseInt(data.split(":")[1].trim());

                                            bis = new BufferedInputStream(in);
                                            buffer = new byte[len];

                                            int t = 0;
                                            while (t < len) {
                                                t += bis.read(buffer, t, len - t);
                                            }

                                            Bytes2ImageFile(buffer, getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");

                                            final Bitmap responseBitmap = BitmapFactory.decodeFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");
                                            Matrix matrix = new Matrix();

                                            matrix.postRotate(90);
                                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(responseBitmap, responseBitmap.getWidth(), responseBitmap.getHeight(), true);

                                            currentFrameBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);


                                        }


                                    }
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (bis != null) {
                                    bis.close();
                                }
                                if (fos != null) {
                                    fos.close();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();


        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void Bytes2ImageFile(byte[] bytes, String fileName) {
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes, 0, bytes.length);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
