package com.smartremote.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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
    private Handler reconnectHandler;
    private Runnable reconnectRunnable;

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
        this.reconnectHandler = new Handler(Looper.getMainLooper());
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
            Log.i("HID", "App Registered: " + registered + " Plugged: " + (pluggedDevice != null ? pluggedDevice.getName() : "null"));
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

        @Override
        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            Log.i("HID", "Host asked for GET_REPORT type:" + type + " id:" + id);
            if (hidDevice != null) {
                // Windows *must* receive a SUCCESS with valid-length dummy data upon connection. 
                // If we send UNSUPPORTED, Windows assumes the driver is broken and disconnects.
                if (id == 1) {
                    hidDevice.replyReport(device, type, id, new byte[8]); // Keyboard dummy
                } else if (id == 2) {
                    hidDevice.replyReport(device, type, id, new byte[4]); // Mouse dummy
                } else {
                    hidDevice.replyReport(device, type, id, new byte[bufferSize]);
                }
            }
        }

        @Override
        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            Log.i("HID", "Host asked for SET_REPORT type:" + type + " id:" + id);
            if (hidDevice != null) {
                // Always acknowledge SET_REPORT (like NumLock LEDs) as success
                hidDevice.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS);
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void registerApp() {
        if (hidDevice != null && !isAppRegistered) {
            
            BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                    "Smart Keyboard Pro", 
                    "Bluetooth HID Input Device",
                    "Generic", // Do not use "Android", Windows sometimes rejects it
                    (byte) 0x40, // KEYBOARD subclass 
                    HidDescriptor.KEYBOARD_MOUSE_REPORT_MAP
            );
            
            try {
                hidDevice.unregisterApp();
            } catch (Exception ignored) {}

            // Executors.newCachedThreadPool() is sometimes better than SingleThread for HID IO
            boolean success = hidDevice.registerApp(sdp, null, null, Executors.newCachedThreadPool(), callback);
            Log.d("HID", "Register status (0x40 Keyboard): " + success);
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
            try {
                hidDevice.sendReport(connectedDevice, id, data);
            } catch (Exception e) {
                Log.e("HID", "Error sending report ID: " + id, e);
            }
        }
    }
}
