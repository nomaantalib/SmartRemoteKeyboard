package com.smartremote.bluetooth;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MouseManager {

    private byte buttons = 0;
    private final ScheduledExecutorService executor;

    public MouseManager() {
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void moveCursor(float dx, float dy) {
        sendMouseReport(buttons, (int) dx, (int) dy, 0);
    }
    
    public void scroll(float dy) {
        // dy is negated because scrolling down on a screen usually means Wheel Down (negative in HID)
        sendMouseReport(buttons, 0, 0, (int) -dy);
    }
    
    public void setButtons(byte btnState) {
        this.buttons = btnState;
        sendMouseReport(buttons, 0, 0, 0);
    }
    
    // Left click: bit 0
    public void clickLeft() {
        pulseButton((byte) 1);
    }
    
    // Right click: bit 1
    public void clickRight() {
        pulseButton((byte) 2);
    }

    private void pulseButton(byte buttonMask) {
        // Press
        setButtons(buttonMask);
        // Release asynchronously after 15ms (matches fast hardware debounce rates)
        executor.schedule(() -> setButtons((byte) 0), 15, TimeUnit.MILLISECONDS);
    }

    // Left hold for dragging
    public void holdLeft(boolean down) {
        if (down) {
            buttons |= 1;
        } else {
            buttons &= ~1;
        }
        sendMouseReport(buttons, 0, 0, 0);
    }

    private void sendMouseReport(byte btn, int dx, int dy, int wheel) {
        byte[] report = new byte[4];
        report[0] = btn;
        
        // Clamp values strictly to standard HID relative mouse limits (-127 to 127)
        report[1] = (byte) Math.max(-127, Math.min(127, dx));
        report[2] = (byte) Math.max(-127, Math.min(127, dy));
        report[3] = (byte) Math.max(-127, Math.min(127, wheel));
        
        HidController hid = HidController.get();
        if (hid != null && hid.isConnected()) {
            hid.sendReport(2, report); // Report ID 2 is Mouse
        }
    }
}
