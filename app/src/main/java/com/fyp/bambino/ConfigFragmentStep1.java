package com.fyp.bambino;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
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

import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigFragmentStep1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigFragmentStep1 extends Fragment {

    private ProgressBar bluetoothProgressBar;
    private RadioGroup bluetoothDevicesRadioGroup;
    private ImageButton refreshButton;
    private TextView tvNoDevicesFound;
    private boolean noDevicesFound = true;
    private Button connectButton;
    private TextView tvFeedbackMessage;
    public static BluetoothManager bluetoothManager;
    public static BluetoothAdapter bluetoothAdapter;

    public static BluetoothSocket socket = null;

    private Set<BluetoothDevice> pairedDevices;
    private Set<BluetoothDevice> scannedDevices;

    private static final int BLUETOOTH_CONNECT_REQUEST_CODE = 1;
    private static final int COARSE_LOCATION_REQUEST_CODE = 2;
    private static final int BLUETOOTH_SCAN_REQUEST_CODE = 3;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothDevice currentBluetoothDevice;

    private boolean isScanReceiverRegistered = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ConfigFragmentStep1() {
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
    public static ConfigFragmentStep1 newInstance(String param1, String param2) {
        ConfigFragmentStep1 fragment = new ConfigFragmentStep1();
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
        View rootView = inflater.inflate(R.layout.fragment_config_step1, container, false);

        initUI(rootView);
        requestLocationPermission();
        setUpBluetooth();
        turnOnBluetooth();
        registerBluetoothStateReceiver();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isScanReceiverRegistered)
            this.getActivity().unregisterReceiver(scanReceiver);
    }

    private void initUI(View view) {
        this.bluetoothProgressBar = view.findViewById(R.id.bluetooth_progress_bar);
        this.bluetoothDevicesRadioGroup = view.findViewById(R.id.radio_group_bluetooth);
        this.refreshButton = view.findViewById(R.id.btn_refresh);
        this.tvNoDevicesFound = view.findViewById(R.id.tv_no_devices_found);
        this.connectButton = view.findViewById(R.id.btn_connect);
        this.tvFeedbackMessage = view.findViewById(R.id.tv_feedback_message);
        this.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.BLUETOOTH_SCAN},
                            BLUETOOTH_SCAN_REQUEST_CODE);
                }
                bluetoothAdapter.startDiscovery();
            }
        });
        this.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentBluetoothDevice != null) {
                    disableConnectButton();
                    showSuccessFeedbackMessage("Connecting...");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                        BLUETOOTH_CONNECT_REQUEST_CODE);
                            }

                            try {

                                socket = currentBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                                socket.connect();
                                OutputStream outputStream = socket.getOutputStream();
                                outputStream.write("Bambino App Connected!".getBytes());

                                ((MainActivity) getActivity()).goToConfigFragmentStep2();

                            } catch (IOException e) {
                                enableConnectButton();
//                                Toast.makeText(getContext(), "Failed to connect to this device!", Toast.LENGTH_LONG).show();
                                showErrorFeedbackMessage("Connection Failed!");
                            }
                        }
                    }, 100);

                }
            }
        });
    }

    private void requestLocationPermission() {

        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (!isGranted) {
                        // Permission denied, handle the case accordingly
                        closeApp();
                    }
                });
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestBlueToothScanPermission() {
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    BLUETOOTH_SCAN_REQUEST_CODE);

        }
        bluetoothAdapter.startDiscovery();

        IntentFilter scanFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.getActivity().registerReceiver(scanReceiver, scanFilter);

        IntentFilter finishDiscoveryFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.getActivity().registerReceiver(scanReceiver, finishDiscoveryFilter);

        IntentFilter startDiscoveryFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        this.getActivity().registerReceiver(scanReceiver, startDiscoveryFilter);

        isScanReceiverRegistered = true;
    }

    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                showProgressBar();
                removeAllRadioButtons(bluetoothDevicesRadioGroup);
                noDevicesFound = true;
                hideTVNoDevicesFound();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // A new Bluetooth device has been discovered
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            BLUETOOTH_CONNECT_REQUEST_CODE);
                }
                noDevicesFound = false;
                hideTVNoDevicesFound();
                renderScannedDevice(device);
                // Do something with the device
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (noDevicesFound) {
                    Log.i("TV:   ", String.valueOf(tvNoDevicesFound));
                    tvNoDevicesFound.setTextColor(ContextCompat.getColor(getContext(), R.color.gray));
                    tvNoDevicesFound.setVisibility(View.VISIBLE);
                    showTVNoDevicesFound();
                }
                hideProgressBar();
            }
        }
    };

    private void removeAllRadioButtons(RadioGroup radioGroup) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View view = radioGroup.getChildAt(i);
            if (view instanceof RadioButton) {
                radioGroup.removeView(view);
                i--; // Decrement i so that we don't skip over the next view
            }
        }
    }

    private void renderScannedDevice(BluetoothDevice device) {
        RadioButton radioButton = new RadioButton(this.getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    BLUETOOTH_CONNECT_REQUEST_CODE);
        }
        if (device != null && device.getName() != null && !device.getName().equals("")) {
            radioButton.setText(device.getName()); // or device.getAddress() depending on what you want to display
            radioButton.setTag(device); // set the tag to the BluetoothDevice object to identify the selected device later
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT));
            radioButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(this.getContext(), R.color.beige)));
            radioButton.setTypeface(ResourcesCompat.getFont(this.getContext(), R.font.gotham_book));
            radioButton.setTextColor(ContextCompat.getColor(this.getContext(), R.color.gray));
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            radioButton.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        // RadioButton is checked
                        RadioButton checkedRadioButton = (RadioButton) compoundButton;
                        BluetoothDevice device = (BluetoothDevice) checkedRadioButton.getTag();
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                    BLUETOOTH_CONNECT_REQUEST_CODE);
                        }
                        if (device.getName().equals("Bambino")) {
                            currentBluetoothDevice = device;
                            enableConnectButton();
                        } else {
                            currentBluetoothDevice = null;
                            disableConnectButton();
                        }

                    }
                }
            });
            bluetoothDevicesRadioGroup.addView(radioButton);
        }

    }

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
                    requestBlueToothScanPermission();

                } else {
                    //Deny pressed -> close the app
                    closeApp();
                }
            });
            enableBtLauncher.launch(enableBTIntent);
        } else {
            //Bluetooth already turned On
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

    private void showProgressBar() {
        this.bluetoothProgressBar.setVisibility(View.VISIBLE);
    }

    private void showTVNoDevicesFound() {
        this.tvNoDevicesFound.setVisibility(View.VISIBLE);
    }

    private void hideTVNoDevicesFound() {
        this.tvNoDevicesFound.setVisibility(View.GONE);
    }

    private void enableConnectButton() {
        this.connectButton.setEnabled(true);
    }

    private void disableConnectButton() {
        this.connectButton.setEnabled(false);
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
            }
            case COARSE_LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted, you can now access the coarse location
                } else {
                    // permission denied, you cannot access the coarse location
                    closeApp();
                }
            }
            case BLUETOOTH_SCAN_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted
                } else {
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
        if (this.getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
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
    }

    private void closeApp() {
        this.getActivity().finish();
    }


    private void showSuccessFeedbackMessage(String message) {
        this.tvFeedbackMessage.setText(message);
        this.tvFeedbackMessage.setTextColor(ContextCompat.getColor(this.getContext(), R.color.green));
        this.tvFeedbackMessage.setVisibility(View.VISIBLE);
    }


    private void showErrorFeedbackMessage(String message) {
        this.tvFeedbackMessage.setText(message);
        this.tvFeedbackMessage.setTextColor(ContextCompat.getColor(this.getContext(), R.color.red));
        this.tvFeedbackMessage.setVisibility(View.VISIBLE);
    }

    private void hideFeedbackMessage() {
        this.tvFeedbackMessage.setVisibility(View.GONE);
    }
}