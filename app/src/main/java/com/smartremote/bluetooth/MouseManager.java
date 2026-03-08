package com.smartremote.bluetooth;

public class MouseManager {

    private byte buttons = 0;

    public void moveCursor(float dx, float dy) {
        sendMouseReport(buttons, (int)dx, (int)dy, 0);
    }
    
    public void scroll(float dy) {
        // Wheel is usually sent on the 4th byte
        // A scroll event doesn't move mouse, so dx and dy are 0
        sendMouseReport(buttons, 0, 0, (int)dy);
    }
    
    public void setButtons(byte btnState) {
        this.buttons = btnState;
        sendMouseReport(buttons, 0, 0, 0);
    }
    
    public void clickLeft() {
        setButtons((byte)1); // byte 0 bit 0
        try { Thread.sleep(20); } catch (Exception e){} // small delay
        setButtons((byte)0);
    }
    
    public void clickRight() {
        setButtons((byte)2); // byte 0 bit 1
        try { Thread.sleep(20); } catch (Exception e){}
        setButtons((byte)0);
    }

    private void sendMouseReport(byte btn, int dx, int dy, int wheel) {
        byte[] report = new byte[4];
        report[0] = btn;
        
        // Clamp values to HID limits (-127 to 127 for typical descriptors)
        report[1] = (byte) Math.max(-127, Math.min(127, dx));
        report[2] = (byte) Math.max(-127, Math.min(127, dy));
        report[3] = (byte) Math.max(-127, Math.min(127, wheel));
        
        HidController hid = HidController.get();
        if (hid != null) {
            hid.sendReport(2, report); // Report ID 2 is Mouse
        }
    }
}
