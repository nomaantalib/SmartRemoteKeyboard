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
            // Unregister first if possible to clear stale states
            try {
                hidDevice.unregisterApp();
            } catch (Exception e) {
                Log.w("HID", "Failed to unregister app (might not be registered): " + e.getMessage());
            }

            BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                    "SmartRemote HID",
                    "Smart Remote Keyboard & Mouse",
                    "Google",
                    (byte) 0x00, // Use generic subclass for better compatibility
                    HidDescriptor.KEYBOARD_MOUSE_REPORT_MAP
            );
            
            BluetoothHidDevice.Callback callback = new BluetoothHidDevice.Callback() {
                @Override
                public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
                    isAppRegistered = registered;
                    Log.i("HID", "App Registered Status: " + registered + (pluggedDevice != null ? " for " + pluggedDevice.getName() : ""));
                }

                @Override
                public void onConnectionStateChanged(BluetoothDevice device, int state) {
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        connectedDevice = device;
                        Log.i("HID", "CONNECTED: " + device.getName() + " (" + device.getAddress() + ")");
                    } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.w("HID", "DISCONNECTED: " + (device != null ? device.getName() : "Unknown device"));
                        if (connectedDevice != null && (device == null || connectedDevice.equals(device))) {
                            connectedDevice = null;
                        }
                    }
                }
            };
            
            boolean result = hidDevice.registerApp(sdp, null, null, Executors.newSingleThreadExecutor(), callback);
            Log.d("HID", "HID App Registration Triggered: " + result);
        }
    }

    public boolean isConnected() {
        return connectedDevice != null;
    }

    public String getConnectedDeviceName() {
        if (connectedDevice == null) return "None";
        try {
            return connectedDevice.getName();
        } catch (SecurityException e) {
            return "Connected Device";
        }
    }

    @SuppressLint("MissingPermission")
    public void sendReport(int id, byte[] data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && hidDevice != null && connectedDevice != null) {
            try {
                boolean success = hidDevice.sendReport(connectedDevice, id, data);
                if (!success) {
                    Log.w("HID", "Report failed to send - possibly disconnected");
                }
            } catch (Exception e) {
                Log.e("HID", "Error sending HID report", e);
            }
        }
    }
}
