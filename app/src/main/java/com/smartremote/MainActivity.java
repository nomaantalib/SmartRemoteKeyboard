package com.smartremote;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;

import com.smartremote.network.NetworkManager;
import com.smartremote.discovery.ServerDiscovery;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private TextView statusText;
    private NetworkManager networkManager;
    private ServerDiscovery serverDiscovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        MaterialCardView cardKeyboard = findViewById(R.id.cardKeyboard);
        MaterialCardView cardTouchpad = findViewById(R.id.cardTouchpad);
        MaterialCardView cardFullRemote = findViewById(R.id.cardFullRemote);
        Button btnConnect = findViewById(R.id.btnConnect);
        Button btnHelp = findViewById(R.id.btnHelp);

        SwitchMaterial switchWifi = findViewById(R.id.switchWifi);
        TextInputEditText etIpAddress = findViewById(R.id.etIpAddress);
        networkManager = NetworkManager.getInstance();
        serverDiscovery = new ServerDiscovery();

        switchWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String ip = etIpAddress.getText().toString().trim();
                if (!ip.isEmpty()) {
                    networkManager.connect(ip);
                    statusText.setText("Wi-Fi Mode: Connecting to " + ip + "...");
                    statusText.setTextColor(0xFF4CAF50);
                    Toast.makeText(this, "Wi-Fi Mode Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    // Try auto-discovery first
                    statusText.setText("Scanning network for server...");
                    serverDiscovery.discover(new ServerDiscovery.DiscoveryListener() {
                        @Override
                        public void onServerFound(String foundIp, String name) {
                            runOnUiThread(() -> {
                                etIpAddress.setText(foundIp);
                                networkManager.connect(foundIp);
                                statusText.setText("Auto-connected to: " + name + " (" + foundIp + ")");
                                statusText.setTextColor(0xFF4CAF50);
                                Toast.makeText(MainActivity.this, "Server found: " + name, Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onDiscoveryFailed(String reason) {
                            runOnUiThread(() -> {
                                switchWifi.setChecked(false);
                                statusText.setText("No server found. Enter IP manually.");
                                Toast.makeText(MainActivity.this, reason, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            } else {
                networkManager.setWifiMode(false);
                statusText.setText("Status: Ready");
                statusText.setTextColor(0xFFAAAAAA);
                Toast.makeText(this, "Wi-Fi Mode Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        cardKeyboard.setOnClickListener(v -> startActivity(new Intent(this, KeyboardActivity.class)));
        cardTouchpad.setOnClickListener(v -> startActivity(new Intent(this, TouchpadActivity.class)));
        cardFullRemote.setOnClickListener(v -> startActivity(new Intent(this, FullRemoteActivity.class)));
        
        btnConnect.setOnClickListener(v -> checkPermissionsAndInitBluetooth());
        btnHelp.setOnClickListener(v -> startActivity(new Intent(this, TutorialActivity.class)));
    }


    private void checkPermissionsAndInitBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] permissions = {
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN
            };
            boolean allGranted = true;
            for (String p : permissions) {
                if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        initBluetooth();
    }

    private void initBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Initialize and Start the background HidService to keep connection active
        Intent serviceIntent = new Intent(this, com.smartremote.bluetooth.HidService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        statusText.setText("Status: HID Server Started\nCRITICAL: If connection fails, 'Unpair' your phone on Windows and try again.");
        Toast.makeText(this, "IMPORTANT: Connection is now persistent via background service.", Toast.LENGTH_LONG).show();
        
        // Request Android to make this phone discoverable
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600); // 10 minutes
        startActivity(discoverableIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            initBluetooth();
        }
    }
}
