package com.smartremote.bluetooth;

import com.smartremote.input.KeyMapper;

public class KeyboardManager {

    public void sendKey(String key) {
        byte[] reportDown = new byte[8];
        reportDown[2] = KeyMapper.getKeyCode(key);
        
        HidController hid = HidController.get();
        if (hid != null) {
            // Send Key Down
            hid.sendReport(1, reportDown);
            
            // Send Key Up (Empty Report)
            byte[] reportUp = new byte[8];
            hid.sendReport(1, reportUp);
        }
    }
    
    // Support modifier keys like shift if added later
    public void sendModifierAndKey(byte modifier, String key) {
        byte[] reportDown = new byte[8];
        reportDown[0] = modifier;
        reportDown[2] = KeyMapper.getKeyCode(key);
        
        HidController hid = HidController.get();
        if (hid != null) {
            hid.sendReport(1, reportDown);
            byte[] reportUp = new byte[8];
            hid.sendReport(1, reportUp);
        }
    }
}
