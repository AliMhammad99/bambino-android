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
        setUpBluetooth();
        turnOnBluetooth();

        return rootView;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    private void initUI(View view) {
        this.bluetoothProgressBar = view.findViewById(R.id.bluetooth_progress_bar);
        this.bluetoothDevicesRadioGroup = view.findViewById(R.id.radio_group_bluetooth);
    }

    private void setUpBluetooth() {
        this.bluetoothManager = this.getActivity().getSystemService(BluetoothManager.class);
        this.bluetoothAdapter = this.bluetoothManager.getAdapter();
        if (this.bluetoothAdapter == null) {
            //Device does not support Bluetooth -> Close the app
            this.getActivity().finish();
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
                } else {
                    //Deny pressed -> close the app
                    this.getActivity().finish();
                }
            });
            enableBtLauncher.launch(enableBTIntent);
        } else {
            //Bluetooth already turned On
            getPairedBluetoothDevices();
            hideProgressBar();
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
                        RadioGroup.LayoutParams.WRAP_CONTENT,
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
                    getActivity().finish();
                }
            }
            default: {
                return;
            }

        }
    }
}