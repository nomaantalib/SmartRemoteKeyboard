package com.smartremote.bluetooth;

import com.smartremote.input.KeyMapper;
import com.smartremote.network.NetworkManager;
import com.smartremote.network.PacketBuilder;
import com.smartremote.utils.Constants;

public class KeyboardManager {

    public void sendKey(String key) {
        byte keycode = KeyMapper.getKeyCode(key);
        byte[] reportDown = new byte[8];
        reportDown[2] = keycode;
        
        HidController hid = HidController.get();
        if (hid != null) {
            hid.sendReport(1, reportDown);
        }
        
        // Also send via direct Wi-Fi binary protocol
        NetworkManager net = NetworkManager.getInstance();
        if (net.isWifiMode()) {
            net.send(PacketBuilder.keyPress(keycode));
        }
    }
    
    public void sendKeyRelease() {
        byte[] reportUp = new byte[8];
        HidController hid = HidController.get();
        if (hid != null) {
            hid.sendReport(1, reportUp);
        }
        
        // Also release via Wi-Fi
        NetworkManager net = NetworkManager.getInstance();
        if (net.isWifiMode()) {
            net.send(PacketBuilder.keyRelease((byte) 0));
        }
    }
    
    public void sendModifierAndKey(byte modifier, String key) {
        byte keycode = KeyMapper.getKeyCode(key);
        byte[] reportDown = new byte[8];
        reportDown[0] = modifier;
        reportDown[2] = keycode;
        
        HidController hid = HidController.get();
        if (hid != null) {
            hid.sendReport(1, reportDown);
        }
        
        NetworkManager net = NetworkManager.getInstance();
        if (net.isWifiMode()) {
            net.send(PacketBuilder.keyPress(keycode));
        }
    }
    
    /**
     * Send a media control command (play/pause, volume, etc.)
     * Only works over Wi-Fi mode since Bluetooth HID Consumer Control
     * requires a separate HID descriptor.
     */
    public void sendMediaKey(byte mediaCode) {
        NetworkManager net = NetworkManager.getInstance();
        if (net.isWifiMode()) {
            byte[] packet = new byte[]{Constants.EVENT_MEDIA, mediaCode, 0, 0};
            net.send(packet);
        }
    }
}
