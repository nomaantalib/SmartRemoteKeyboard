package com.smartremote.network;

import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager {
    private static final String TAG = "NetworkManager";
    private static NetworkManager instance;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort = 9999;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isWifiMode = false;

    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    private NetworkManager() {}

    public void connect(String ip, int port) {
        executor.execute(() -> {
            try {
                serverAddress = InetAddress.getByName(ip);
                serverPort = port;
                if (socket == null || socket.isClosed()) {
                    socket = new DatagramSocket();
                }
                isWifiMode = true;
                Log.d(TAG, "Connected to Wi-Fi server: " + ip + ":" + port);
            } catch (IOException e) {
                Log.e(TAG, "Connection failed", e);
            }
        });
    }

    public void setWifiMode(boolean enabled) {
        this.isWifiMode = enabled;
    }

    public boolean isWifiMode() {
        return isWifiMode;
    }

    public void sendData(String data) {
        if (!isWifiMode || serverAddress == null || socket == null) return;

        executor.execute(() -> {
            try {
                byte[] buffer = data.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
                socket.send(packet);
            } catch (IOException e) {
                Log.e(TAG, "Failed to send data", e);
            }
        });
    }

    public void sendHidReport(int reportId, byte[] data) {
        if (!isWifiMode) return;
        
        // Protocol: "ID:BYTE1,BYTE2,..."
        StringBuilder sb = new StringBuilder();
        sb.append(reportId).append(":");
        for (int i = 0; i < data.length; i++) {
            sb.append(data[i]);
            if (i < data.length - 1) sb.append(",");
        }
        sendData(sb.toString());
    }
}
