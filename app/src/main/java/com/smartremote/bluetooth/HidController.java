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
            stopReconnectLoop();
        }
    }

    private final BluetoothHidDevice.Callback callback = new BluetoothHidDevice.Callback() {
        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            isAppRegistered = registered;
            Log.i("HID", "App Registered: " + registered + " Plugged: " + (pluggedDevice != null ? pluggedDevice.getName() : "null"));
            
            if (registered && hidDevice != null) {
                if (pluggedDevice != null) {
                    try { hidDevice.connect(pluggedDevice); } catch (Exception e) {}
                }
                startReconnectLoop();
            } else {
                stopReconnectLoop();
            }
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            if (state == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device;
                Log.i("HID", "CONNECTED to " + device.getName());
                stopReconnectLoop(); // Successfully connected, stop retrying
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w("HID", "DISCONNECTED from " + (device != null ? device.getName() : "device"));
                if (connectedDevice != null && (device == null || connectedDevice.equals(device))) {
                    connectedDevice = null;
                    startReconnectLoop(); // Dropped, start retrying
                }
            }
        }

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
                hidDevice.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS);
            }
        }
    };

    private void startReconnectLoop() {
        if (reconnectRunnable != null) return;
        
        reconnectRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isConnected() && hidDevice != null && isAppRegistered) {
                    try {
                        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                        if (adapter != null) {
                            for (BluetoothDevice device : adapter.getBondedDevices()) {
                                Log.d("HID", "Attempting auto-reconnect to: " + device.getName());
                                hidDevice.connect(device); // Connect to known hosts
                            }
                        }
                    } catch (Exception e) {
                        Log.e("HID", "Reconnect error", e);
                    }
                    reconnectHandler.postDelayed(this, 5000); // Retry every 5 seconds
                } else {
                    reconnectRunnable = null; // Stop if connected or unregistered
                }
            }
        };
        reconnectHandler.post(reconnectRunnable);
    }

    private void stopReconnectLoop() {
        if (reconnectRunnable != null) {
            reconnectHandler.removeCallbacks(reconnectRunnable);
            reconnectRunnable = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void registerApp() {
        if (hidDevice != null && !isAppRegistered) {
            
            // Subclass 0x40 specifically means "Keyboard" (0x80 is Mouse, 0xC0 is Combo).
            // Many Windows hosts reject combos if the SDP name doesn't match the descriptor.
            BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                    "Smart Remote System", // Less generic, prevents Windows caching conflicts
                    "Bluetooth HID Input Device",
                    "Android HID",
                    (byte) 0x40, // KEYBOARD subclass to satisfy strict host rules
                    HidDescriptor.KEYBOARD_MOUSE_REPORT_MAP
            );
            
            try {
                hidDevice.unregisterApp();
                Log.d("HID", "Unregistering old app state before new registration.");
            } catch (Exception ignored) {}

            boolean success = hidDevice.registerApp(sdp, null, null, Executors.newSingleThreadExecutor(), callback);
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
