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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LiveVideoRemoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LiveVideoRemoteFragment extends Fragment {
    private ImageView imageView;
    private RequestQueue requestQueue;
    private String imageUrl = "https://bambinoserver0.000webhostapp.com/image.jpg";
    private String getFlashLEDUrl = "https://bambinoserver0.000webhostapp.com/get_flash_led.php";
    private String setFlashLEDUrl = "https://bambinoserver0.000webhostapp.com/set_flash_led.php?flashLED=";
    private ProgressBar progressBar;
    private ImageButton flashButton;
    private boolean flashOn = false;
    Timer liveVideoTimer;
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
        this.liveVideoTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (LiveVideoService.connectionLost) {

                            showProgressBar();
                            hideFlashButton();
                            return;
                        }
                        // Render image
                        imageView.setImageBitmap(LiveVideoService.currentFrameBitmap);
                        hideProgressBar();
                        showFlashButton();

                    }
                });
            }
        };
        // Schedule the timer to run every 1 second
        this.liveVideoTimer.schedule(timerTask, 0, 500);

        this.flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flashOn = !flashOn;
                setFlashLEDOnServer(flashOn);
                if (flashOn) {

                    flashButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flash_on));
                } else {
                    flashButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flash_off));
                }
            }
        });
        return rootView;
    }


    private void setFlashLEDOnServer(boolean flashLED) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, setFlashLEDUrl + flashLED, null, null);
        requestQueue.add(stringRequest);
    }

    private void getFlashLEDFromServer() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getFlashLEDUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        boolean flashOn = Boolean.parseBoolean(response);
                        if (flashOn) {
                            flashButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flash_on));
                        } else {
                            flashButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flash_off));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
            }
        });
        requestQueue.add(stringRequest);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (this.liveVideoTimer != null) {
            this.liveVideoTimer.cancel();
            this.liveVideoTimer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.liveVideoTimer != null) {
            this.liveVideoTimer.cancel();
            this.liveVideoTimer = null;
        }
    }
}