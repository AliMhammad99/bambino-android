package com.fyp.bambino;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LiveVideoLocalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LiveVideoLocalFragment extends Fragment {
    private FragmentActivity fragmentActivity;
    private ImageView ivLiveVideo;
    private ImageButton flashButton;
    private ProgressBar progressBar;
    private boolean flashOn = false;

    private boolean updatingFlashState = false;

    private String localURL = "http://192.168.0.107:80";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LiveVideoLocalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LiveVideoLocalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LiveVideoLocalFragment newInstance(String param1, String param2) {
        LiveVideoLocalFragment fragment = new LiveVideoLocalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_live_video_local, container, false);
        this.ivLiveVideo = rootView.findViewById(R.id.live_video);


        StreamThread streamThread = new StreamThread();
        Thread thread = new Thread(streamThread);
        thread.start();

//        FlashLEDThread flashLEDThread = new FlashLEDThread();
//        Thread thread1 = new Thread(flashLEDThread);
//        thread1.start();

        this.flashButton = rootView.findViewById(R.id.btn_flash);
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!LiveVideoLocalService.flashUpdating) {
                    LiveVideoLocalService.flashUpdating = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // Perform network operation here
                            if (flashOn) {
                                turnFlashOff();
                                flashOn = false;
                            } else {
                                turnFlashOn();
                                flashOn = true;
                            }
                        }
                    }).start();


                }
            }
        });
        fragmentActivity = getActivity();
        this.progressBar = rootView.findViewById(R.id.progressBar);

        return rootView;
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


    private class StreamThread implements Runnable {

        @Override
        public void run() {
            while (true) {


                if (fragmentActivity != null) {

                    final Bitmap responseBitmap = BitmapFactory.decodeFile(fragmentActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");
//                    Matrix matrix = new Matrix();
//
//                    matrix.postRotate(90);
                    if (responseBitmap != null) {
//                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(responseBitmap, responseBitmap.getWidth(), responseBitmap.getHeight(), true);
//
//                        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                        fragmentActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressBar();
                                ivLiveVideo.setImageBitmap(LiveVideoLocalService.currentFrameBitmap);
                            }
                        });
                    }
                }
//                }
            }


        }


    }


    private class FlashLEDThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                if (LiveVideoLocalService.flashUpdating) {
                    flashOn ^= true;

                    String flash_url;
                    if (flashOn) {
                        flash_url = localURL + "/flash_on";
                    } else {
                        flash_url = localURL + "/flash_off";
                    }

                    try {

                        URL url = new URL(flash_url);

                        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                        huc.setRequestMethod("GET");
                        huc.setConnectTimeout(1000 * 5);
                        huc.setReadTimeout(1000 * 5);
                        huc.setDoInput(true);
                        huc.connect();
                        if (flashOn) {
                            flashButton.setImageResource(R.drawable.ic_flash_on);
                        } else {
                            flashButton.setImageResource(R.drawable.ic_flash_off);
                        }
//                        huc.getResponseCode();
                        if (huc.getResponseCode() == 200) {
                            InputStream in = huc.getInputStream();

                            InputStreamReader isr = new InputStreamReader(in);
                            BufferedReader br = new BufferedReader(isr);
                            huc.disconnect();
                            LiveVideoLocalService.flashUpdating = false;
                        }

                    } catch (Exception e) {
//                        LiveVideoLocalService.flashUpdating = false;
                        e.printStackTrace();
                    }

                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
////                    throw new RuntimeException(e);
//                }
            }
        }

    }


    private void showProgressBar() {
        this.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        this.progressBar.setVisibility(View.GONE);
    }

    private void turnFlashOn() {
        if (LiveVideoLocalService.flashUpdating) {

            String flash_url = localURL + "/flash_on";

            try {

                URL url = new URL(flash_url);

                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setRequestMethod("GET");
                huc.setConnectTimeout(1000 * 5);
                huc.setReadTimeout(1000 * 5);
                huc.setDoInput(true);
                huc.connect();
                if (huc.getResponseCode() == 200) {
                    flashButton.setImageResource(R.drawable.ic_flash_on);
                    huc.disconnect();
                    LiveVideoLocalService.flashUpdating = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void turnFlashOff() {
        if (LiveVideoLocalService.flashUpdating) {

            String flash_url = localURL + "/flash_off";

            try {

                URL url = new URL(flash_url);

                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setRequestMethod("GET");
                huc.setConnectTimeout(1000 * 5);
                huc.setReadTimeout(1000 * 5);
                huc.setDoInput(true);
                huc.connect();
                if (huc.getResponseCode() == 200) {
                    flashButton.setImageResource(R.drawable.ic_flash_off);
                    huc.disconnect();
                    LiveVideoLocalService.flashUpdating = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
