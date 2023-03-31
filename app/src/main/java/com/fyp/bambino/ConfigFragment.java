package com.fyp.bambino;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigFragment extends Fragment {

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private Set<BluetoothDevice> scannedDevices;
    private static final int BLUETOOTH_CONNECT_REQUEST_CODE = 1;
    private static final int BLUETOOTH_SCAN_REQUEST_CODE = 2;
    private static final int LOCATION_REQUEST_CODE = 3;
    private boolean blueToothIsOn = false;
    private BluetoothStateReceiver bluetoothStateReceiver;
    private BroadcastReceiver bluetoothScanReceiver;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ConfigFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConfigFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfigFragment newInstance(String param1, String param2) {
        ConfigFragment fragment = new ConfigFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("TEST: -------------------", "_------------------------------");
        View rootView = inflater.inflate(R.layout.fragment_config, container, false);
        setupBlueTooth();
        turnOnBlueTooth();

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothStateReceiver = new BluetoothStateReceiver();
        getActivity().registerReceiver(bluetoothStateReceiver, filter);
//        while(!blueToothIsOn);
        Log.i("BlueTooth", String.valueOf(blueToothIsOn));
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN}, BLUETOOTH_SCAN_REQUEST_CODE);
        }

        
        Log.i("Starting SCAN","!!!!!!");
        bluetoothAdapter.startDiscovery();
        bluetoothScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // A Bluetooth device has been found
                    Log.i("A Bluetooth device has been found", "!!!!!!!");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_CONNECT_REQUEST_CODE);
                    }
                    Log.i("Found device: ", device.getName() + " - " + device.getAddress());
                }
            }
        };
        IntentFilter bluetoothScanFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(bluetoothScanReceiver, bluetoothScanFilter);
//        scanBlueToothDevices();
//        getPairedDevices();

        return rootView;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Unregister the BroadcastReceiver
        getActivity().unregisterReceiver(bluetoothStateReceiver);
        getActivity().unregisterReceiver(bluetoothScanReceiver);
    }

    private void setupBlueTooth() {
        this.bluetoothManager = (BluetoothManager) getActivity().getSystemService(BluetoothManager.class);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            //Device does not support bluetooth
            getActivity().finish();
        }
        Log.i("BlueTooth Setup Done", "!!!!");
    }

    private void turnOnBlueTooth() {

        if (!bluetoothAdapter.isEnabled()) {
            // Create an intent to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            // Register an activity result callback for the Bluetooth enable request
            ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Bluetooth was enabled successfully
                    blueToothIsOn = true;
                    getPairedDevices();

                    if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // Permission is not granted, request it
                        ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
                        // You should handle the result in onRequestPermissionsResult()

                    }
//                    bluetoothAdapter.startDiscovery();
                } else {
                    // Bluetooth was not enabled
                    getActivity().finish();
                }
            });

            // Launch the Bluetooth enable request activity
            enableBtLauncher.launch(enableBtIntent);
        } else {
            blueToothIsOn = true;
            getPairedDevices();
        }
    }



    private class BluetoothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    // Bluetooth has been turned off
                    Log.i("Bluetooth Turned OFF", "!!!");
                    blueToothIsOn = false;
//                    getActivity().finish();
                    exitAppPopUpMessage();
                } else if (state == BluetoothAdapter.STATE_ON) {
                    // Bluetooth has been turned off
                    Log.i("Bluetooth Turned ON", "!!!");
                    blueToothIsOn = true;
                }
            }
        }
    }

    private void exitAppPopUpMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setMessage("You turned Off bluetooth, Bambino will close now.");
        builder.setTitle("Bluetooth Turned Off");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do something when the OK button is clicked
                getActivity().finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case BLUETOOTH_CONNECT_REQUEST_CODE:
            case BLUETOOTH_SCAN_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission denied. You cannot access Bluetooth features.
                    // Exit the app
                    getActivity().finish();
                }
                return;
            }
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted, start the Bluetooth device discovery process
                    if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN}, BLUETOOTH_SCAN_REQUEST_CODE);
                    }
                    Log.i("Starting SCAN","!!!!!!");
                    bluetoothAdapter.startDiscovery();
                } else {
                    // Permission is denied, display a message to the user and exit the app
                    Toast.makeText(this.getActivity(), "Location permission is required to discover Bluetooth devices.", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
                return;

        }
    }

    private void getPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_CONNECT_REQUEST_CODE);

        }
        this.pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.i("Size:  ", String.valueOf(this.pairedDevices.size()));
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.i(deviceName + ":  ", deviceHardwareAddress);
            }
        }
    }

    private void scanBlueToothDevices() {
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        this.getActivity().registerReceiver(receiver, filter);
        if (!bluetoothAdapter.isEnabled()) {
            // Create an intent to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            // Register an activity result callback for the Bluetooth enable request
            ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Bluetooth was enabled successfully
                    if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_SCAN_REQUEST_CODE);

                    }
//                    bluetoothAdapter.startDiscovery();
                } else {
                    // Bluetooth was not enabled
                    getActivity().finish();
                }
            });

            // Launch the Bluetooth enable request activity
            enableBtLauncher.launch(enableBtIntent);

        }
    }
}