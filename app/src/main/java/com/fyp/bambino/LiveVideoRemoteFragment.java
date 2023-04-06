package com.fyp.bambino;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LiveVideoRemoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LiveVideoRemoteFragment extends Fragment {
//    private Handler handler;
//    private Runnable runnable;


    private ImageView imageView;
    private RequestQueue requestQueue;
    private String imageUrl = "https://bambinoserver0.000webhostapp.com/image.jpg";
    private String getFlashLEDUrl = "https://bambinoserver0.000webhostapp.com/get_flash_led.php";
    private String setFlashLEDUrl = "https://bambinoserver0.000webhostapp.com/set_flash_led.php?flashLED=";

    private ProgressBar progressBar;

    private ImageButton flashButton;
    private boolean flashOn = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LiveVideoRemoteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LiveVideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LiveVideoRemoteFragment newInstance(String param1, String param2) {
        LiveVideoRemoteFragment fragment = new LiveVideoRemoteFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_live_video_remote, container, false);

        imageView = rootView.findViewById(R.id.live_video);
        requestQueue = Volley.newRequestQueue(this.getContext());

        progressBar = rootView.findViewById(R.id.progressBar);

        this.flashButton = rootView.findViewById(R.id.btn_flash);

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Call your PHP server here
                        getImageFromServer();
                    }
                });
            }
        };
        // Schedule the timer to run every 1 second
        timer.schedule(timerTask, 0, 1000);

        this.flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flashOn) {
                    flashButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flash_off));
                } else {
                    flashButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flash_on));
                }
            }
        });

        // Create the handler and runnable for the repeating task
//        handler = new Handler();
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                Log.d("MyFragment", "Hello, world!");
//                handler.postDelayed(this, 1000);
//            }
//        };
//
//        // Start the repeating task
//        handler.postDelayed(runnable, 1000);
        // Inflate the layout for this fragment
        return rootView;

    }

    private void getImageFromServer() {
        ImageRequest imageRequest = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap responseBitmap) {

                        Matrix matrix = new Matrix();

                        matrix.postRotate(90);

                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(responseBitmap, responseBitmap.getWidth(), responseBitmap.getHeight(), true);

                        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                        hideProgressBar();

                        imageView.setImageBitmap(rotatedBitmap);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showProgressBar();
                    }
                });

        requestQueue.add(imageRequest);
    }
//    @Override
//    public void onPause() {
//        super.onPause();
//        // Stop the repeating task when the fragment is no longer visible
//        handler.removeCallbacks(runnable);
//    }

    private void showProgressBar(){
        this.progressBar.setVisibility(View.VISIBLE);
    }
    private void hideProgressBar(){
        this.progressBar.setVisibility(View.GONE);
    }
}