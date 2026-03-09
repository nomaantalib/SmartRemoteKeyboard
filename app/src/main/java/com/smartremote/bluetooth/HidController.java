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
                // Removed the 30s ping — it clogs the HID connection queue 
                // and increases latency for actual keypresses.
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
            Log.d("HID", "App Registered: " + registered + " Plugged: " + (pluggedDevice != null ? pluggedDevice.getName() : "null"));
            
            // Wait strictly for onAppStatusChanged = true before auto-connecting
            if (registered && hidDevice != null && pluggedDevice != null) {
                connect(pluggedDevice);
            }
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            if (state == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device;
                Log.d("HID", "CONNECTED to " + device.getName());
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w("HID", "DISCONNECTED from " + (device != null ? device.getName() : "device"));
                if (connectedDevice != null && (device == null || connectedDevice.equals(device))) {
                    connectedDevice = null;
                }
                
                // Reconnect Automatically - Self-healing connection
                if (device != null && hidDevice != null && isAppRegistered) {
                    Log.d("HID", "Attempting auto-reconnect as per STATE_DISCONNECTED...");
                    hidDevice.connect(device); // Auto reconnect
                }
            }
        }

        // Pre-allocate to prevent Garbage Collection pauses during typing/handshaking
        private final byte[] dummyKeyboard = new byte[8];
        private final byte[] dummyMouse = new byte[4];

        @Override
        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            Log.i("HID", "Host asked for GET_REPORT type:" + type + " id:" + id);
            if (hidDevice != null) {
                if (id == 1) {
                    hidDevice.replyReport(device, type, id, dummyKeyboard); 
                } else if (id == 2) {
                    hidDevice.replyReport(device, type, id, dummyMouse); 
                } else {
                    hidDevice.replyReport(device, type, id, new byte[bufferSize]);
                }
            }
        }


        @Override
        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            Log.i("HID", "Host asked for SET_REPORT type:" + type + " id:" + id);
            if (hidDevice != null) {
                hidDevice.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS);
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void registerApp() {
        if (hidDevice != null && !isAppRegistered) {
            
            // Production-ready SDP settings
            BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                    "Smart Remote Keyboard", 
                    "Bluetooth HID Keyboard",
                    "SmartRemote", 
                    BluetoothHidDevice.SUBCLASS1_COMBO, // Better than 0x40/0x00 for cross-compatibility
                    HidDescriptor.KEYBOARD_MOUSE_REPORT_MAP
            );
            
            try {
                hidDevice.unregisterApp();
            } catch (Exception ignored) {}

            // Must be SingleThreadExecutor! If Cached is used, Android might send Key Up before Key Down
            // resulting in stuck keys or dropped inputs on the Windows side.
            boolean success = hidDevice.registerApp(sdp, null, null, Executors.newSingleThreadExecutor(), callback);
            Log.d("HID", "Register status: " + success);
        }
    }
    
    // Explicit API to force bond and connect from the UI
    @SuppressLint("MissingPermission")
    public void connect(BluetoothDevice device) {
        if (hidDevice != null && isAppRegistered) {
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                Log.d("HID", "Device not bonded, initiating bond...");
                device.createBond();
            } else {
                Log.d("HID", "Device bonded, connecting HID profile...");
                hidDevice.connect(device);
            }
        } else {
            Log.w("HID", "Cannot connect to device. HID Profile ready: " + (hidDevice != null) + ", App Registered: " + isAppRegistered);
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
        // Fallback: Also send via Wi-Fi if enabled
        com.smartremote.network.NetworkManager.getInstance().sendHidReport(id, data);

        if (hidDevice != null && connectedDevice != null && isAppRegistered) {
            try {
                hidDevice.sendReport(connectedDevice, id, data);
            } catch (Exception e) {
                Log.e("HID", "Error sending report ID: " + id, e);
            }
        }
    }
}
