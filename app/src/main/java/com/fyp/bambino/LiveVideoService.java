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
import android.util.Base64;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class LiveVideoService extends Service {


    int counter;
    RemoteViews customForegroundNotificationView;
    Notification.Builder foregroundNotification;

    public static Bitmap currentFrameBitmap = null;
    public static boolean flashUpdating = false;
    public static boolean connectionLost = true;
    private String localURL = "http://192.168.43.239:80";
    private String remoteUrl = "https://bambinoserver0.000webhostapp.com/image.jpg";
    private Timer remoteTimer;
    private RequestQueue requestQueue;
    private boolean shouldStop = false;
    private Context context = this;
    public static final int NORMAL = 0;
    public static final int DANGER = 1;
    public static final int NO_DATA = 2;

    private String mode = "";

    private Timer flaskAPITimer;
    private String flaskAPIURL = "https://3be1-34-142-220-199.ngrok-free.app/upload";

    public static boolean emergencyCallRunning = false;

    public static int stateCell1 = NO_DATA;
    public static int stateCell2 = NO_DATA;
    public static int stateCell3 = NO_DATA;
    public static int stateCell4 = NO_DATA;

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        counter = 0;
        if ("stop".equals(intent.getAction())) {
            Log.i("STOPPPPPPPPPP", "STOPPPPPPPPPP");
            stop();
        } else if ("local".equals(intent.getAction())) {
//            stop();
            Log.i("LOCALLLLLLLLLLLLL", "LOCALLLLLLLLLLLLL");
            mode = "Local";
            startLocalLiveVideo();
            connectToFlaskServer();
        } else if ("remote".equals(intent.getAction())) {
            Log.i("REMOTEEEEEEEEEEEE", "REMOTEEEEEEEEEEEE");
//            stop();
            mode = "Remote";
            startRemoteLiveVideo();
            connectToFlaskServer();
        }
        setUpForegroundNotification();
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

        //Set its title based on mode
        this.customForegroundNotificationView.setTextViewText(R.id.notification_title, "Bambino is monitoring your baby (" + mode + ")");

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
        stateCell1 = cell1;
        stateCell2 = cell2;
        stateCell3 = cell3;
        stateCell4 = cell4;
        switch (cell1) {
            case NORMAL:
                setImageViewInsideForegroundNotification("dashboard_cell1", R.drawable.ic_dashboard_notification_incrib);
                break;
            case DANGER:
                setImageViewInsideForegroundNotification("dashboard_cell1", R.drawable.ic_dashboard_notification_nobaby);
                break;
            case NO_DATA:
                setImageViewInsideForegroundNotification("dashboard_cell1", R.drawable.ic_dashboard_notification_nodata);
                break;
        }
        switch (cell2) {
            case NORMAL:
                setImageViewInsideForegroundNotification("dashboard_cell2", R.drawable.ic_dashboard_notification_onback);
                break;
            case DANGER:
                setImageViewInsideForegroundNotification("dashboard_cell2", R.drawable.ic_dashboard_notification_onface);
                break;
            case NO_DATA:
                setImageViewInsideForegroundNotification("dashboard_cell2", R.drawable.ic_dashboard_notification_nodata);
                break;
        }
        switch (cell3) {
            case NORMAL:
                setImageViewInsideForegroundNotification("dashboard_cell3", R.drawable.ic_dashboard_notification_covered);
                break;
            case DANGER:
                setImageViewInsideForegroundNotification("dashboard_cell3", R.drawable.ic_dashboard_notification_uncovered);
                break;
            case NO_DATA:
                setImageViewInsideForegroundNotification("dashboard_cell3", R.drawable.ic_dashboard_notification_nodata);
                break;
        }
        switch (cell4) {
            case NORMAL:
                setImageViewInsideForegroundNotification("dashboard_cell4", R.drawable.ic_dashboard_notification_sleeping);
                break;
            case DANGER:
                setImageViewInsideForegroundNotification("dashboard_cell4", R.drawable.ic_dashboard_notification_awake);
                break;
            case NO_DATA:
                setImageViewInsideForegroundNotification("dashboard_cell4", R.drawable.ic_dashboard_notification_nodata);
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
        connectionLost = true;
        stopForeground(true);
        stopSelf();
        if (this.remoteTimer != null) {
            this.remoteTimer.cancel();
            this.remoteTimer = null;
        }
        if (this.flaskAPITimer != null) {
            this.flaskAPITimer.cancel();
            this.flaskAPITimer = null;
        }
    }

    private void startEmergencyCall() {
        // Launch your activity
        emergencyCallRunning = true;
        Intent activityIntent = new Intent(context, EmergencyCallActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activityIntent);
    }

    private void startLocalLiveVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (remoteTimer != null) {
                    remoteTimer.cancel();
                    remoteTimer = null;
                }
                while (!shouldStop) {
                    if (!flashUpdating) {
                        counter++;
                        Log.i("COUNTER:  ", String.valueOf(counter));
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

    private void connectToFlaskServer() {
        flaskAPITimer = new Timer();
//        currentFrameBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // your code here
                if (currentFrameBitmap != null && !connectionLost) {
                    Log.i("FlaskThread:    ", "RUNNING");
                    // Perform network operations here
                    // Load the Bitmap from a drawable resource

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    currentFrameBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    URL url = null;
                    HttpURLConnection connection = null;
                    try {
                        url = new URL(flaskAPIURL);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestProperty("Accept", "application/json");

                        JSONObject jsonRequest = new JSONObject();
                        jsonRequest.put("image", encodedImage);

                        OutputStream outputStream = connection.getOutputStream();
                        outputStream.write(jsonRequest.toString().getBytes());
                        outputStream.flush();
                        outputStream.close();

                        int statusCode = connection.getResponseCode();
                        Log.i("RESPONSE CODE:   ", String.valueOf(statusCode));
                        if (statusCode == HttpURLConnection.HTTP_OK) {
//                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                String line;
                                String response = "";
                                while ((line = in.readLine()) != null) {
                                    response += line;
                                }
                                Log.i("RESPONSE:    ", response);
                                in.close();
                                JSONArray jsonArray = new JSONArray(response); //replace responseString with the actual response from the API
                                int[] result = new int[jsonArray.length()];
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    result[i] = jsonArray.getInt(i);
                                }
                                updateForegroundNotification(result[0], result[1], result[2], result[3]);
                                if (result[1] == DANGER && !emergencyCallRunning) {
                                    startEmergencyCall();
                                }
                            }
                        } else {
                            updateForegroundNotification(NO_DATA, NO_DATA, NO_DATA, NO_DATA);
                        }

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            }
        };
        // Schedule the timer to run every 2 seconds
        flaskAPITimer.schedule(timerTask, 0, 1000);
    }

}
