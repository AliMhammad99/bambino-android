package com.fyp.bambino;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

    private ProgressBar bluetoothProgressBar;
    private RadioGroup bluetoothDevicesRadioGroup;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private Set<BluetoothDevice> pairedDevices;

    private static final int BLUETOOTH_CONNECT_REQUEST_CODE = 1;
    private static final int COARSE_LOCATION_REQUEST_CODE = 2;
    private static final int BLUETOOTH_SCAN_REQUEST_CODE = 3;

    String[] permissions = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION};
    int MY_PERMISSIONS_REQUEST_CODE = 123;


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
        View rootView = inflater.inflate(R.layout.fragment_config, container, false);

        initUI(rootView);
        requestLocationPermission();
//        requestBlueToothScanPermission();
        setUpBluetooth();
        turnOnBluetooth();
        registerBluetoothStateReceiver();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.getActivity().unregisterReceiver(scanReceiver);
    }

    private void initUI(View view) {
        this.bluetoothProgressBar = view.findViewById(R.id.bluetooth_progress_bar);
        this.bluetoothDevicesRadioGroup = view.findViewById(R.id.radio_group_bluetooth);
    }

    private void requestLocationPermission() {
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Permission granted, do your work here
                        Log.i("GRANTED NOW!!!!!!!", "");
                    } else {
                        // Permission denied, handle the case accordingly
                        closeApp();
                    }
                });
//        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestBlueToothScanPermission() {
        Log.i("SCANNING.............................", "");
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.i("BLUETOOTH_SCAN NOT GRANTED.............................", "");
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    BLUETOOTH_SCAN_REQUEST_CODE);

        }
        bluetoothAdapter.startDiscovery();


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.getActivity().registerReceiver(scanReceiver, filter);
    }

    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("DEVICE FOUND.............................", "");
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // A new Bluetooth device has been discovered
                Log.i("DEVICE FOUND.............................", "");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            BLUETOOTH_CONNECT_REQUEST_CODE);
                }
                Log.i("BLUETOOTH DEVICE:    ", device.getName());
                // Do something with the device
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("SCAN FINISHED.............................", "");
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    Log.i("BLUETOOTH_SCAN NOT GRANTED.............................", "");
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.BLUETOOTH_SCAN},
                            BLUETOOTH_SCAN_REQUEST_CODE);

                }
                bluetoothAdapter.startDiscovery();
            }
        }
    };

    private void setUpBluetooth() {
        this.bluetoothManager = this.getActivity().getSystemService(BluetoothManager.class);
        this.bluetoothAdapter = this.bluetoothManager.getAdapter();
        if (this.bluetoothAdapter == null) {
            //Device does not support Bluetooth -> Close the app
            closeApp();
        }
    }



    private void turnOnBluetooth() {
        if (!this.bluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    //Allow pressed
                    getPairedBluetoothDevices();
                    hideProgressBar();
                    requestBlueToothScanPermission();

                } else {
                    //Deny pressed -> close the app
                    closeApp();
                }
            });
            enableBtLauncher.launch(enableBTIntent);
        } else {
            //Bluetooth already turned On
            getPairedBluetoothDevices();
            hideProgressBar();
            requestBlueToothScanPermission();
        }
    }

    private void getPairedBluetoothDevices() {
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_CONNECT_REQUEST_CODE);
        }
        this.pairedDevices = this.bluetoothAdapter.getBondedDevices();
        if (this.pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                RadioButton radioButton = new RadioButton(this.getContext());
                radioButton.setText(device.getName()); // or device.getAddress() depending on what you want to display
                radioButton.setTag(device); // set the tag to the BluetoothDevice object to identify the selected device later
                radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT));
                radioButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(this.getContext(), R.color.beige)));
                radioButton.setTypeface(ResourcesCompat.getFont(this.getContext(), R.font.gotham_book));
                radioButton.setTextColor(ContextCompat.getColor(this.getContext(), R.color.gray));
                radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                bluetoothDevicesRadioGroup.addView(radioButton);
            }
        }
    }

    private void hideProgressBar() {
        this.bluetoothProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case BLUETOOTH_CONNECT_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission denied. You cannot access Bluetooth features.
                    // Exit the app
                    closeApp();
                }
                Log.i("BLUETOOTH_CONNECT_REQUEST_CODE", " GRANTED");
            }
            case COARSE_LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted, you can now access the coarse location
                    Log.i("LOCATION NOW ", "GRANTED");
                } else {
                    Log.i("LOCATION DENIED ", "");
                    // permission denied, you cannot access the coarse location
                    closeApp();
                }
            }
            case BLUETOOTH_SCAN_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted
                    Log.i("HELLO SCAN", "");
                } else {
                    Log.i("NOT HELLO SCAN", "");
                    // Permission has been denied
                    closeApp();
                }
            }
            default: {
                return;
            }

        }
    }

    private void registerBluetoothStateReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        BluetoothStateReceiver bluetoothStateReceiver = new BluetoothStateReceiver();
        this.getActivity().registerReceiver(bluetoothStateReceiver, filter);
    }

    private class BluetoothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    // Bluetooth has been turned off
                    exitAppPopUpMessage();
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
                closeApp();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void closeApp() {
        this.getActivity().finish();
    }
}