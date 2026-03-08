package com.smartremote.bluetooth;

public class MouseManager {

    public void moveCursor(float dx,float dy){

        byte[] report = new byte[3];

        report[0] = 0;
        report[1] = (byte)dx;
        report[2] = (byte)dy;

        // send HID mouse report

    }

}
