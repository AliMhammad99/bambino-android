package com.fyp.bambino;

import static com.fyp.bambino.LiveVideoService.DANGER;
import static com.fyp.bambino.LiveVideoService.NORMAL;
import static com.fyp.bambino.LiveVideoService.NO_DATA;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
//        startDashboardUpdater();
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
                updateDashboard();
            }
        };
        // Schedule the timer to run every 2 seconds
        this.updateDashBoardTimer.schedule(timerTask, 0, 300);
    }


    private void updateDashboard() {

        switch (LiveVideoService.stateCell1) {
            case NORMAL:
                this.ivCell1.setImageResource(R.drawable.ic_dashboard_notification_incrib);
                break;
            case DANGER:
                this.ivCell1.setImageResource(R.drawable.ic_dashboard_notification_nobaby);
                break;
            case NO_DATA:
                this.ivCell1.setImageResource(R.drawable.ic_dashboard_notification_nodata);
                break;
        }
        switch (LiveVideoService.stateCell2) {
            case NORMAL:
                this.ivCell2.setImageResource(R.drawable.ic_dashboard_notification_onback);
                break;
            case DANGER:
                this.ivCell2.setImageResource(R.drawable.ic_dashboard_notification_onface);
                break;
            case NO_DATA:
                this.ivCell2.setImageResource(R.drawable.ic_dashboard_notification_nodata);
                break;
        }
        switch (LiveVideoService.stateCell3) {
            case NORMAL:
                this.ivCell3.setImageResource(R.drawable.ic_dashboard_notification_covered);
                break;
            case DANGER:
                this.ivCell3.setImageResource(R.drawable.ic_dashboard_notification_uncovered);
                break;
            case NO_DATA:
                this.ivCell3.setImageResource(R.drawable.ic_dashboard_notification_nodata);
                break;
        }
        switch (LiveVideoService.stateCell4) {
            case NORMAL:
                this.ivCell4.setImageResource(R.drawable.ic_dashboard_notification_sleeping);
                break;
            case DANGER:
                this.ivCell4.setImageResource(R.drawable.ic_dashboard_awake);
                break;
            case NO_DATA:
                this.ivCell4.setImageResource(R.drawable.ic_dashboard_notification_nodata);
                break;
        }
    }

}