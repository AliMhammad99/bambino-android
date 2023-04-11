package com.fyp.bambino;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigFragmentStep2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigFragmentStep2 extends Fragment {

    private EditText etWiFiName;
    private EditText etWiFiPassword;
    private ImageButton showPasswordButton;
    private Spinner modeSpinner;
    private Button confirmButton;
    private TextView tvFeedbackMessage;

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
        initUI(rootView);
        registerBluetoothStateReceiver();
        return rootView;
    }

    private void initUI(View view) {
        this.etWiFiName = view.findViewById(R.id.et_wifi_name);
        this.etWiFiName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Do something before the text is changed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Check if the EditText is not empty
//                if (etWiFiName.getText().length() != 0) {
//                    // Do something if the EditText is not empty
//
//                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do something after the text has been changed
                if (inputsEmpty()) {
                    disableConfirmButton();
                } else {
                    enableConfirmButton();
                }
            }
        });
        this.etWiFiPassword = view.findViewById(R.id.et_wifi_password);
        this.etWiFiPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Do something before the text is changed
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Check if the EditText is not empty
//                if (etWiFiName.getText().length() != 0) {
//                    // Do something if the EditText is not empty
//
//                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do something after the text has been changed
                if (inputsEmpty()) {
                    disableConfirmButton();
                } else {
                    enableConfirmButton();
                }
            }
        });

        this.showPasswordButton = view.findViewById(R.id.btn_show_password);
        this.showPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int inputType = etWiFiPassword.getInputType();
                int newInputType = inputType ^ InputType.TYPE_TEXT_VARIATION_PASSWORD;
                int cursorPosition = etWiFiPassword.getSelectionEnd(); // Save cursor position
                etWiFiPassword.setInputType(newInputType);
                showPasswordButton.setImageDrawable(ContextCompat.getDrawable(getContext(),
                        (newInputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) == InputType.TYPE_TEXT_VARIATION_PASSWORD ?
                                R.drawable.ic_password_hidden : R.drawable.ic_password_visible));
                etWiFiPassword.setTypeface(ResourcesCompat.getFont(getContext(), R.font.gotham_medium));
                etWiFiPassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                etWiFiPassword.setSelection(cursorPosition); // Restore cursor position
            }

        });

        this.modeSpinner = view.findViewById(R.id.spinner_mode);
        final List<String> states = Arrays.asList("Local", "Remote");

        // Our custom Adapter class that we created
        SpinnerAdapter adapter = new ModeSpinnerAdapter(this.getContext(), states);
        this.modeSpinner.setAdapter(adapter);
        this.modeSpinner.setPopupBackgroundResource(R.color.white);

        this.confirmButton = view.findViewById(R.id.btn_confirm);
        this.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableConfirmButton();
                showSuccessFeedbackMessage("Confirming...");
                String wifiName = etWiFiName.getText().toString() + " \n";
                String wifiPassword = etWiFiPassword.getText().toString() + " \n";
                String mode = String.valueOf(modeSpinner.getSelectedItemPosition()) + " \n";

                OutputStream outputStream = null;
                showSuccessFeedbackMessage("Successful!");
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("bambino", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Log.i("MODE:  ", String.valueOf(modeSpinner.getSelectedItemPosition()));
                editor.putString("mode", String.valueOf(modeSpinner.getSelectedItemPosition()));
                editor.apply();
                ((MainActivity) getActivity()).setMode(String.valueOf(modeSpinner.getSelectedItemPosition()));
                ((MainActivity) getActivity()).updateMode();
//                try {
//
//                    outputStream = ConfigFragmentStep1.socket.getOutputStream();
//                    outputStream.write(wifiName.getBytes());
//                    outputStream.write(wifiPassword.getBytes());
//                    outputStream.write(mode.getBytes());
//                    showSuccessFeedbackMessage("Successful!");
//                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("bambino", Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    Log.i("MODE:  ", String.valueOf(modeSpinner.getSelectedItemPosition()));
//                    editor.putString("mode", String.valueOf(modeSpinner.getSelectedItemPosition()));
//                    editor.apply();
//                    ((MainActivity) getActivity()).setMode(String.valueOf(modeSpinner.getSelectedItemPosition()));
//                    ((MainActivity) getActivity()).updateMode();
//                } catch (IOException e) {
//                    showErrorFeedbackMessage("Connection Failed!");
//                    enableConfirmButton();
//                }

            }
        });

        this.tvFeedbackMessage = view.findViewById(R.id.tv_feedback_message);
    }

    private boolean inputsEmpty() {
        return this.etWiFiName.getText().length() == 0 || this.etWiFiPassword.getText().length() == 0;
    }

    private void enableConfirmButton() {
        this.confirmButton.setEnabled(true);
    }

    private void disableConfirmButton() {
        this.confirmButton.setEnabled(false);
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
}