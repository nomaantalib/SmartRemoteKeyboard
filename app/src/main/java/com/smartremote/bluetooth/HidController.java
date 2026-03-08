package com.smartremote.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.concurrent.Executors;

@SuppressLint("MissingPermission")
public class HidController implements BluetoothProfile.ServiceListener {
    
    private static HidController instance;
    private BluetoothHidDevice hidDevice;
    private BluetoothDevice connectedDevice;
    private boolean isAppRegistered = false;
    private Context context;

    public static HidController getInstance(Context context) {
        if (instance == null) {
            instance = new HidController(context.getApplicationContext());
        }
        return instance;
    }
    
    public static HidController get() {
        return instance;
    }

    private HidController(Context context) {
        this.context = context;
    }

    public void initialize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && adapter.isEnabled()) {
                adapter.getProfileProxy(context, this, BluetoothProfile.HID_DEVICE);
            }
        }
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && profile == BluetoothProfile.HID_DEVICE) {
            hidDevice = (BluetoothHidDevice) proxy;
            registerApp();
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && profile == BluetoothProfile.HID_DEVICE) {
            hidDevice = null;
            isAppRegistered = false;
        }
    }

    @SuppressLint("MissingPermission")
    private void registerApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && hidDevice != null && !isAppRegistered) {
            BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                    "Smart Remote",
                    "A Smart Remote Keyboard & Mouse",
                    "SmartApp",
                    (byte) 0xC0, // Combo device
                    HidDescriptor.KEYBOARD_MOUSE_REPORT_MAP
            );
            
            BluetoothHidDevice.Callback callback = new BluetoothHidDevice.Callback() {
                @Override
                public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
                    isAppRegistered = registered;
                    Log.i("HID", "App Registered: " + registered);
                    if (registered) {
                        // Attempt to connect to any already bonded device that supports HID
                        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                        for (BluetoothDevice device : adapter.getBondedDevices()) {
                            hidDevice.connect(device);
                        }
                    }
                }

                @Override
                public void onConnectionStateChanged(BluetoothDevice device, int state) {
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        connectedDevice = device;
                        Log.i("HID", "Device Connected: " + device.getName());
                    } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        if (connectedDevice != null && connectedDevice.equals(device)) {
                            connectedDevice = null;
                        }
                    }
                }
            };
            
            hidDevice.registerApp(sdp, null, null, Executors.newSingleThreadExecutor(), callback);
        }
    }

    @SuppressLint("MissingPermission")
    public void sendReport(int id, byte[] data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && hidDevice != null && connectedDevice != null) {
            hidDevice.sendReport(connectedDevice, id, data);
        }
    }
}
