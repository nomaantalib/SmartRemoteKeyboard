package com.smartremote.bluetooth;

import com.smartremote.input.KeyMapper;

public class KeyboardManager {

    public void sendKey(String key){

        byte[] report = new byte[8];

        report[2] = KeyMapper.getKeyCode(key);

        // send HID report

    }

}
