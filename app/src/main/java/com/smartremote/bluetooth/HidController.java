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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
                
                // Keep-alive mechanism
                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                    if (isConnected()) {
                        Log.d("HID", "Stability Check: Connected to " + getConnectedDeviceName());
                    }
                }, 30, 30, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        if (profile == BluetoothProfile.HID_DEVICE) {
            hidDevice = (BluetoothHidDevice) proxy;
            registerApp();
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        if (profile == BluetoothProfile.HID_DEVICE) {
            hidDevice = null;
            isAppRegistered = false;
        }
    }

    private final BluetoothHidDevice.Callback callback = new BluetoothHidDevice.Callback() {
        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            isAppRegistered = registered;
            Log.i("HID", "App Registered: " + registered);
            if (registered && hidDevice != null) {
                // Auto-reconnect without needing to unpair
                if (pluggedDevice != null) {
                    try { hidDevice.connect(pluggedDevice); } catch (Exception e) {}
                } else {
                    try {
                        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                        if (adapter != null) {
                            for (BluetoothDevice device : adapter.getBondedDevices()) {
                                hidDevice.connect(device); // Connect to known hosts
                            }
                        }
                    } catch (Exception e) {}
                }
            }
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            if (state == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device;
                Log.i("HID", "CONNECTED to " + device.getName());
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w("HID", "DISCONNECTED from " + (device != null ? device.getName() : "device"));
                if (connectedDevice != null && (device == null || connectedDevice.equals(device))) {
                    connectedDevice = null;
                }
            }
        }

        // CRITICAL: Windows sends GET_REPORT/SET_REPORT immediately upon connection. 
        // If we don't reply, Windows will instantly drop the connection.
        @Override
        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            Log.i("HID", "Host asked for Report ID: " + id);
            if (hidDevice != null) {
                if (id == 1) hidDevice.replyReport(device, type, id, new byte[8]);
                else if (id == 2) hidDevice.replyReport(device, type, id, new byte[4]);
                else hidDevice.replyReport(device, type, id, new byte[bufferSize]);
            }
        }

        @Override
        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            Log.i("HID", "Host set Report ID: " + id);
            if (hidDevice != null) {
                // Acknowledge the host's report (e.g., NumLock/CapsLock LED states)
                hidDevice.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS);
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void registerApp() {
        if (hidDevice != null && !isAppRegistered) {
            BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                    "Smart Remote",
                    "BT HID Keyboard/Mouse",
                    "SmartRemote",
                    (byte) 0x00, // Generic
                    HidDescriptor.KEYBOARD_MOUSE_REPORT_MAP
            );
            
            try {
                hidDevice.unregisterApp();
            } catch (Exception ignored) {}

            boolean success = hidDevice.registerApp(sdp, null, null, Executors.newSingleThreadExecutor(), callback);
            Log.d("HID", "Register status: " + success);
        }
    }

    public boolean isConnected() {
        return connectedDevice != null;
    }

    public String getConnectedDeviceName() {
        return connectedDevice != null ? connectedDevice.getName() : "None";
    }

    @SuppressLint("MissingPermission")
    public void sendReport(int id, byte[] data) {
        if (hidDevice != null && connectedDevice != null) {
            hidDevice.sendReport(connectedDevice, id, data);
        }
    }
}
