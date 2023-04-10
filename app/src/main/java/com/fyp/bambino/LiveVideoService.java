package com.fyp.bambino;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class LiveVideoService extends Service {


    int counter;
    RemoteViews customForegroundNotificationView;
    Notification.Builder foregroundNotification;

    public static Bitmap currentFrameBitmap;
    public static boolean flashUpdating = false;
    public static boolean connectionLost = true;
    private String localURL = "http://192.168.43.239:80";
    private String remoteUrl = "https://bambinoserver0.000webhostapp.com/image.jpg";
    private Timer remoteTimer;
    private RequestQueue requestQueue;
    private boolean shouldStop = false;
    private Context context = this;
    final int NORMAL = 0;
    final int DANGER = 1;
    final int NO_DATA = 2;


    @Override
    public void onCreate() {
        super.onCreate();
        setUpForegroundNotification();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        counter = 0;
        if ("stop".equals(intent.getAction())) {
            stop();
        } else if ("local".equals(intent.getAction())) {
            startLocalLiveVideo();
        } else if ("remote".equals(intent.getAction())) {
            startRemoteLiveVideo();
        }

        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setUpForegroundNotification() {
        final String CHANNEL_ID = "Bambino Local Live Video";
        NotificationChannel notificationChannel = null;

        // Create a PendingIntent for when the notification is clicked to open dashboard
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Create custom view for the notification
        this.customForegroundNotificationView = new RemoteViews(getPackageName(), R.layout.layout_live_video_foreground_service);

        //Set stop button intent
        Intent stopIntent = new Intent(this, LiveVideoService.class);
        stopIntent.setAction("stop");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);
        this.customForegroundNotificationView.setOnClickPendingIntent(R.id.btn_stop, stopPendingIntent);

        // Set the padding of the custom view to match the notification height
        this.customForegroundNotificationView.setViewPadding(R.id.notification_layout, 0, 0, 0, 0);

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

    private void updateForegroundNotification(int cell1, int cell2, int cell3, int cell4) {

        switch (cell1) {
            case NORMAL:
                setImageViewInsideForegroundNotification("dashboard_cell1", R.drawable.ic_dashboard_incrib);
                break;
            case DANGER:
                setImageViewInsideForegroundNotification("dashboard_cell1", R.drawable.ic_dashboard_nobaby);
                break;
            case NO_DATA:
                setImageViewInsideForegroundNotification("dashboard_cell1", R.drawable.ic_dashboard_nodata);
                break;
        }
        switch (cell2) {
            case NORMAL:
                setImageViewInsideForegroundNotification("dashboard_cell2", R.drawable.ic_dashboard_onback);
                break;
            case DANGER:
                setImageViewInsideForegroundNotification("dashboard_cell2", R.drawable.ic_dashboard_onface);
                break;
            case NO_DATA:
                setImageViewInsideForegroundNotification("dashboard_cell2", R.drawable.ic_dashboard_nodata);
                break;
        }
        switch (cell3) {
            case NORMAL:
                setImageViewInsideForegroundNotification("dashboard_cell3", R.drawable.ic_dashboard_covered);
                break;
            case DANGER:
                setImageViewInsideForegroundNotification("dashboard_cell3", R.drawable.ic_dashboard_uncovered);
                break;
            case NO_DATA:
                setImageViewInsideForegroundNotification("dashboard_cell3", R.drawable.ic_dashboard_nodata);
                break;
        }
        switch (cell4) {
            case NORMAL:
                setImageViewInsideForegroundNotification("dashboard_cell4", R.drawable.ic_dashboard_sleeping);
                break;
            case DANGER:
                setImageViewInsideForegroundNotification("dashboard_cell4", R.drawable.ic_dashboard_awake);
                break;
            case NO_DATA:
                setImageViewInsideForegroundNotification("dashboard_cell4", R.drawable.ic_dashboard_nodata);
                break;
        }
        //Hide progress bar
//        this.customForegroundNotificationView.setViewVisibility(R.id.progress_bar, View.GONE);
        this.customForegroundNotificationView.setTextViewText(R.id.data, counter + "");

        this.foregroundNotification.setCustomContentView(customForegroundNotificationView);
        this.startForeground(1001, foregroundNotification.build());
    }

    private void setImageViewInsideForegroundNotification(String imageViewId, int drawableSrcId) {
        //Change an imageview src
        int imageViewIdInt = getResources().getIdentifier(imageViewId, "id", getPackageName());
        this.customForegroundNotificationView.setImageViewResource(imageViewIdInt, drawableSrcId);
    }

    private void hideProgressBar() {
        //Hide progress bar
        this.customForegroundNotificationView.setViewVisibility(R.id.progress_bar, View.GONE);
        this.startForeground(1001, foregroundNotification.build());
    }

    private void showProgressBar() {
        //Show progress bar
        this.customForegroundNotificationView.setViewVisibility(R.id.progress_bar, View.VISIBLE);
        this.startForeground(1001, foregroundNotification.build());
    }

    private void connectionEstablished() {
        connectionLost = false;
        hideProgressBar();
    }

    private void connectionLost() {
        connectionLost = true;
        showProgressBar();
        updateForegroundNotification(NO_DATA, NO_DATA, NO_DATA, NO_DATA);
    }

    private void stop() {
        // Stop the service
        shouldStop = true;
        stopForeground(true);
        stopSelf();
        if (this.remoteTimer != null) {
            this.remoteTimer.cancel();
            this.remoteTimer = null;
        }
    }

    private void startEmergencyCall() {
        // Launch your activity
        Intent activityIntent = new Intent(context, EmergencyCallActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activityIntent);
    }

    private void startLocalLiveVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (!shouldStop) {
                    if (!flashUpdating) {
                        counter++;
                        Log.i("COUNTER:  ", String.valueOf(counter));
                        if (counter == 3) {
                            startEmergencyCall();

                        }
//                            updateForegroundNotification(1, 2, 0, 1);
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
                                    connectionEstablished();
                                    updateForegroundNotification(1, 2, 0, 1);
                                    InputStream in = huc.getInputStream();

                                    InputStreamReader isr = new InputStreamReader(in);
                                    BufferedReader br = new BufferedReader(isr);

                                    String data;

                                    int len;
                                    byte[] buffer;

                                    while ((data = br.readLine()) != null && !flashUpdating && !shouldStop) {
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
                                if (e instanceof SocketTimeoutException || e instanceof ConnectException) {
                                    // Handle SocketTimeoutException
                                    connectionLost();
                                }
                                Thread.sleep(5000);
                                e.printStackTrace();
                            }
                        } catch (MalformedURLException | InterruptedException e) {
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
    }

    private void startRemoteLiveVideo() {
        this.requestQueue = Volley.newRequestQueue(this);
        final Handler handler = new Handler();
        this.remoteTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Call your PHP server here
                        getImageFromServer();
                        Log.i("REMOTE HTTP", " :)");
                    }
                });
            }
        };
        // Schedule the timer to run every 1 second
        this.remoteTimer.schedule(timerTask, 0, 1000);
    }

    private void getImageFromServer() {
        ImageRequest imageRequest = new ImageRequest(remoteUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap responseBitmap) {

                        Matrix matrix = new Matrix();

                        matrix.postRotate(90);

                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(responseBitmap, responseBitmap.getWidth(), responseBitmap.getHeight(), true);

                        currentFrameBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                        connectionEstablished();

                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        connectionLost();
                    }
                });

        requestQueue.add(imageRequest);
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
