package com.smartremote.network;

import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager {
    private static final String TAG = "NetworkManager";
    private static NetworkManager instance;
    private Socket socket;
    private OutputStream output;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isWifiMode = false;
    private String lastIp;

    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    private NetworkManager() {}

    public void connect(String ip) {
        this.lastIp = ip;
        executor.execute(() -> {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                socket = new Socket(ip, 5050);
                output = socket.getOutputStream();
                isWifiMode = true;
                Log.d(TAG, "Connected to TCP server: " + ip + ":5050");
            } catch (IOException e) {
                Log.e(TAG, "TCP connection failed", e);
                isWifiMode = false;
            }
        });
    }

    public void setWifiMode(boolean enabled) {
        this.isWifiMode = enabled;
        if (!enabled) {
            executor.execute(() -> {
                try {
                    if (socket != null) socket.close();
                } catch (IOException ignored) {}
            });
        } else if (lastIp != null) {
            connect(lastIp);
        }
    }

    public boolean isWifiMode() {
        return isWifiMode;
    }

    public void send(byte[] data) {
        if (!isWifiMode || output == null) return;
        
        executor.execute(() -> {
            try {
                output.write(data);
                output.flush();
            } catch (IOException e) {
                Log.e(TAG, "Failed to send packet", e);
                isWifiMode = false; // Disable if socket crashed
            }
        });
    }

    // Helper for sending HID reports over network
    public void sendHidReport(int reportId, byte[] data) {
        if (!isWifiMode) return;
        
        byte[] packet;
        if (reportId == 1) { // Keyboard
            // If data is [modifier, reserved, keycode, ...]
            if (data[2] != 0) {
                 packet = PacketBuilder.keyPress(data[2]);
            } else {
                 // For keyboard, report ID 1 release is usually empty keycodes
                 // We don't have enough info here to release a specific key easy
                 // but let's assume if it's called with 0, it's a release.
                 packet = PacketBuilder.keyRelease((byte) 0);
            }
            send(packet);
        } else if (reportId == 2) { // Mouse
            // data is [btn, dx, dy, wheel]
            packet = PacketBuilder.mouseMove(data[1], data[2]);
            send(packet);
            if (data[0] != 0) {
                send(PacketBuilder.mouseClick(data[0]));
            }
            if (data[3] != 0) {
                send(PacketBuilder.scroll(data[3]));
            }
        }
    }
}
