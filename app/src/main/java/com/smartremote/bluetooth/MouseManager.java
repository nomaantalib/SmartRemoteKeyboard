package com.smartremote.bluetooth;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.smartremote.network.NetworkManager;
import com.smartremote.network.PacketBuilder;

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
        sendMouseReport(buttons, 0, 0, (int) -dy);
    }
    
    public void setButtons(byte btnState) {
        this.buttons = btnState;
        sendMouseReport(buttons, 0, 0, 0);
    }
    
    public void clickLeft() {
        pulseButton((byte) 1);
    }
    
    public void clickRight() {
        pulseButton((byte) 2);
    }

    private void pulseButton(byte buttonMask) {
        setButtons(buttonMask);
        executor.schedule(() -> setButtons((byte) 0), 15, TimeUnit.MILLISECONDS);
    }

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
        report[1] = (byte) Math.max(-127, Math.min(127, dx));
        report[2] = (byte) Math.max(-127, Math.min(127, dy));
        report[3] = (byte) Math.max(-127, Math.min(127, wheel));
        
        // Send via Bluetooth HID
        HidController hid = HidController.get();
        if (hid != null && hid.isConnected()) {
            hid.sendReport(2, report);
        }
        
        // Also send via Wi-Fi binary protocol
        NetworkManager net = NetworkManager.getInstance();
        if (net.isWifiMode()) {
            if (dx != 0 || dy != 0) {
                net.send(PacketBuilder.mouseMove(dx, dy));
            }
            if (btn != 0) {
                net.send(PacketBuilder.mouseClick(btn));
            }
            if (wheel != 0) {
                net.send(PacketBuilder.scroll(wheel));
            }
        }
    }
}
