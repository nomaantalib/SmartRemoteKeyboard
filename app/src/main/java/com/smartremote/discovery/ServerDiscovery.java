package com.smartremote.discovery;

import android.util.Log;
import com.smartremote.utils.Constants;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Discovers the Smart Remote PC server on the local network.
 * Sends a UDP broadcast and listens for the server's response.
 */
public class ServerDiscovery {
    
    private static final String TAG = "ServerDiscovery";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public interface DiscoveryListener {
        void onServerFound(String ip, String name);
        void onDiscoveryFailed(String reason);
    }
    
    /**
     * Scan the current LAN for a Smart Remote server.
     * The server replies with "SMARTREMOTE_SERVER:<hostname>" on UDP 5051.
     */
    public void discover(DiscoveryListener listener) {
        executor.execute(() -> {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                socket.setSoTimeout(3000); // 3 second timeout
                
                // Send broadcast discovery packet
                byte[] sendData = Constants.DISCOVERY_MAGIC.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData, sendData.length,
                        InetAddress.getByName("255.255.255.255"),
                        Constants.DISCOVERY_PORT
                );
                socket.send(sendPacket);
                Log.d(TAG, "Discovery broadcast sent on port " + Constants.DISCOVERY_PORT);
                
                // Wait for response
                byte[] recvBuf = new byte[256];
                DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(recvPacket);
                
                String response = new String(recvPacket.getData(), 0, recvPacket.getLength()).trim();
                String serverIp = recvPacket.getAddress().getHostAddress();
                
                if (response.startsWith(Constants.DISCOVERY_RESPONSE)) {
                    String serverName = response.contains(":")
                            ? response.split(":")[1]
                            : serverIp;
                    Log.d(TAG, "Server found: " + serverName + " at " + serverIp);
                    listener.onServerFound(serverIp, serverName);
                } else {
                    listener.onDiscoveryFailed("Unknown response: " + response);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Discovery failed", e);
                listener.onDiscoveryFailed("No server found on this network");
            } finally {
                if (socket != null) socket.close();
            }
        });
    }
}
