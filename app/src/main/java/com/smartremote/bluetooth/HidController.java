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
                
                // Keep-alive mechanism to prevent idle timeout
                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                    if (isConnected()) {
                        Log.d("HID", "Ping: Keep-alive mouse report");
                        sendReport(2, new byte[4]); // Empty Mouse report
                    }
                }, 60, 60, java.util.concurrent.TimeUnit.SECONDS);
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
                    (byte) 0xC0, // Combo device: Keyboard (0x40) | Mouse (0x80)
                    HidDescriptor.KEYBOARD_MOUSE_REPORT_MAP
            );
            
            BluetoothHidDevice.Callback callback = new BluetoothHidDevice.Callback() {
                @Override
                public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
                    isAppRegistered = registered;
                    Log.i("HID", "App Registered Status: " + registered);
                }

                @Override
                public void onConnectionStateChanged(BluetoothDevice device, int state) {
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        connectedDevice = device;
                        Log.i("HID", "Device Connected: " + device.getName() + " [" + device.getAddress() + "]");
                    } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i("HID", "Device Disconnected: " + device.getName());
                        if (connectedDevice != null && connectedDevice.equals(device)) {
                            connectedDevice = null;
                        }
                    }
                }
            };
            
            boolean result = hidDevice.registerApp(sdp, null, null, Executors.newSingleThreadExecutor(), callback);
            Log.d("HID", "Register App Result: " + result);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && hidDevice != null && connectedDevice != null) {
            try {
                hidDevice.sendReport(connectedDevice, id, data);
            } catch (Exception e) {
                Log.e("HID", "Error sending report", e);
            }
        } else {
            Log.w("HID", "Cannot send report: Device not connected or HID not initialized");
        }
    }
}
