package com.fyp.bambino;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
    private String localURL = "http://192.168.43.239:80";

    Thread threadStream;

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
        fragmentActivity = getActivity();
        this.progressBar = rootView.findViewById(R.id.progressBar);


        StreamThread streamThread = new StreamThread();
        this.threadStream = new Thread(streamThread);
        this.threadStream.start();

        this.flashButton = rootView.findViewById(R.id.btn_flash);
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!LiveVideoService.flashUpdating) {
                    LiveVideoService.flashUpdating = true;
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
        return rootView;
    }

    private class StreamThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                if (LiveVideoService.connectionLost) {

                    fragmentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideFlashButton();
                            showProgressBar();
                        }
                    });
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
                    }
                    continue;
                }
                if(LiveVideoService.emergencyCallRunning){
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
                    }
                    continue;
                }

                File file = new File(fragmentActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "0A.jpg");
                if (file.exists()) {
                    final Bitmap responseBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    // render the bitmap
                    if (responseBitmap != null) {
                        fragmentActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showFlashButton();
                                hideProgressBar();
                                ivLiveVideo.setImageBitmap(LiveVideoService.currentFrameBitmap);
                            }
                        });
                    }
                }

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

    private void hideFlashButton() {
        this.flashButton.setVisibility(View.GONE);
    }

    private void showFlashButton() {
        this.flashButton.setVisibility(View.VISIBLE);
    }

    private void turnFlashOn() {
        if (LiveVideoService.flashUpdating) {

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
                    LiveVideoService.flashUpdating = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void turnFlashOff() {
        if (LiveVideoService.flashUpdating) {

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
                    LiveVideoService.flashUpdating = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.threadStream.interrupt();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.threadStream.interrupt();
    }
}
