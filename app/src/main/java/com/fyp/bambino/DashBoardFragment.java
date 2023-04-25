package com.fyp.bambino;

import static com.fyp.bambino.LiveVideoService.DANGER;
import static com.fyp.bambino.LiveVideoService.NORMAL;
import static com.fyp.bambino.LiveVideoService.NO_DATA;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashBoardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashBoardFragment extends Fragment {
    Timer updateDashBoardTimer;

    private ImageView ivCell1;
    private ImageView ivCell2;
    private ImageView ivCell3;
    private ImageView ivCell4;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DashBoardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashBoardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashBoardFragment newInstance(String param1, String param2) {
        DashBoardFragment fragment = new DashBoardFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_dash_board, container, false);
        initUI(rootView);
        return rootView;
    }

    private void initUI(View view) {
        this.ivCell1 = view.findViewById(R.id.dashboard_cell1);
        this.ivCell2 = view.findViewById(R.id.dashboard_cell2);
        this.ivCell3 = view.findViewById(R.id.dashboard_cell3);
        this.ivCell4 = view.findViewById(R.id.dashboard_cell4);
    }

    private void startDashboardUpdater() {
        this.updateDashBoardTimer = new Timer();
//        currentFrameBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // your code here

            }
        };
        // Schedule the timer to run every 2 seconds
        this.updateDashBoardTimer.schedule(timerTask, 0, 300);
    }


    private void updateDashboard(int cell1, int cell2, int cell3, int cell4) {

        switch (cell1) {
            case NORMAL:
                setDashBoardCell("dashboard_cell1", R.drawable.ic_dashboard_incrib);
                break;
            case DANGER:
                setDashBoardCell("dashboard_cell1", R.drawable.ic_dashboard_nobaby);
                break;
            case NO_DATA:
                setDashBoardCell("dashboard_cell1", R.drawable.ic_dashboard_nodata);
                break;
        }
        switch (cell2) {
            case NORMAL:
                setDashBoardCell("dashboard_cell2", R.drawable.ic_dashboard_onback);
                break;
            case DANGER:
                setDashBoardCell("dashboard_cell2", R.drawable.ic_dashboard_onface);
                break;
            case NO_DATA:
                setDashBoardCell("dashboard_cell2", R.drawable.ic_dashboard_nodata);
                break;
        }
        switch (cell3) {
            case NORMAL:
                setDashBoardCell("dashboard_cell3", R.drawable.ic_dashboard_covered);
                break;
            case DANGER:
                setDashBoardCell("dashboard_cell3", R.drawable.ic_dashboard_uncovered);
                break;
            case NO_DATA:
                setDashBoardCell("dashboard_cell3", R.drawable.ic_dashboard_nodata);
                break;
        }
        switch (cell4) {
            case NORMAL:
                setDashBoardCell("dashboard_cell4", R.drawable.ic_dashboard_sleeping);
                break;
            case DANGER:
                setDashBoardCell("dashboard_cell4", R.drawable.ic_dashboard_awake);
                break;
            case NO_DATA:
                setDashBoardCell("dashboard_cell4", R.drawable.ic_dashboard_nodata);
                break;
        }
        //Hide progress bar
//        this.customForegroundNotificationView.setViewVisibility(R.id.progress_bar, View.GONE);
        this.customForegroundNotificationView.setTextViewText(R.id.data, counter + "");

        this.foregroundNotification.setCustomContentView(customForegroundNotificationView);
        this.startForeground(1001, foregroundNotification.build());
    }

    private void setDashBoardCell(String imageViewId, int drawableSrcId) {
        //Change an imageview src
        int imageViewIdInt = getResources().getIdentifier(imageViewId, "id", getActivity().getPackageName());
        this.customForegroundNotificationView.setImageViewResource(imageViewIdInt, drawableSrcId);
    }
}