package com.fyp.bambino;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigFragmentStep2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigFragmentStep2 extends Fragment {

    private static final int FINE_LOCATION_REQUEST_CODE = 4;
    private static final int CHANGE_WIFI_STATE_REQUEST_CODE = 5;

    private WifiManager wifiManager;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ConfigFragmentStep2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConfigFragmentStep2.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfigFragmentStep2 newInstance(String param1, String param2) {
        ConfigFragmentStep2 fragment = new ConfigFragmentStep2();
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
        View rootView = inflater.inflate(R.layout.fragment_config_step2, container, false);
//        ArrayList scanResults = getAvailableWifiNetworks();
//        for (int i = 0; i < scanResults.size(); i++) {
//            ScanResult scanResult = (ScanResult) scanResults.get(i);
//            Log.i("WifiNetwork", "SSID: " + scanResult.SSID + ", BSSID: " + scanResult.BSSID);
//        }
        return rootView;
    }

    private void setUpWifi(){
        this.wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);

    }

    private void turnOnWifi(){
        Intent enableBTIntent = new Intent(WifiManager.WIFI_STATE_CHANGED_ACTION);
        ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Allow pressed
                        requestBlueToothScanPermission();

                    } else {
                        //Deny pressed -> close the app
                        closeApp();
                    }
        if(this.wifiManager.getWifiState()==WifiManager.WIFI_STATE_DISABLED){
            wifiManager.setWifiEnabled(true);
        }

    }
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        ArrayList<ScanResult> scanResults = getAvailableWifiNetworks();
//        for (ScanResult scanResult : scanResults) {
//            Log.i("WifiNetwork", "SSID: " + scanResult.SSID + ", BSSID: " + scanResult.BSSID);
//        }
//    }
    public ArrayList<ScanResult> getAvailableWifiNetworks() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CHANGE_WIFI_STATE}, CHANGE_WIFI_STATE_REQUEST_CODE);
        }
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);;
        wifiManager.setWifiEnabled(true);
        Log.i("WIFI", String.valueOf(wifiManager.getWifiState()));
        ArrayList<ScanResult> results = new ArrayList<>();
        if (wifiManager != null) {
            wifiManager.startScan();
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        FINE_LOCATION_REQUEST_CODE);
            }
            results = (ArrayList<ScanResult>) wifiManager.getScanResults();
        }
        Log.i("RESULTS", String.valueOf(results));
        return results;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case FINE_LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted, you can now access the coarse location
                } else {
                    // permission denied, you cannot access the coarse location
                    getActivity().finish();
                }
            }
            case CHANGE_WIFI_STATE_REQUEST_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, enable/disable Wi-Fi here
                } else {
                    // Permission denied, handle this case here
                    getActivity().finish();
                }
            }
        }
    }
}