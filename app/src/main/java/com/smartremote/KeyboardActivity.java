package com.smartremote;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.smartremote.bluetooth.HidController;
import com.smartremote.bluetooth.KeyboardManager;

import java.util.HashMap;
import java.util.Map;

public class KeyboardActivity extends AppCompatActivity {

    private KeyboardManager keyboard;
    private TextView statusText;
    private boolean isShiftPressed = false;
    private Handler statusHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);

        keyboard = new KeyboardManager();
        statusText = findViewById(R.id.connectionStatus);

        setupKeys();
        startStatusMonitor();
    }

    private void setupKeys() {
        Map<Integer, String> keyMap = new HashMap<>();
        keyMap.put(R.id.keyQ, "Q"); keyMap.put(R.id.keyW, "W"); keyMap.put(R.id.keyE, "E");
        keyMap.put(R.id.keyR, "R"); keyMap.put(R.id.keyT, "T"); keyMap.put(R.id.keyY, "Y");
        keyMap.put(R.id.keyU, "U"); keyMap.put(R.id.keyI, "I"); keyMap.put(R.id.keyO, "O");
        keyMap.put(R.id.keyP, "P");
        
        keyMap.put(R.id.keyA, "A"); keyMap.put(R.id.keyS, "S"); keyMap.put(R.id.keyD, "D");
        keyMap.put(R.id.keyF, "F"); keyMap.put(R.id.keyG, "G"); keyMap.put(R.id.keyH, "H");
        keyMap.put(R.id.keyJ, "J"); keyMap.put(R.id.keyK, "K"); keyMap.put(R.id.keyL, "L");
        
        keyMap.put(R.id.keyZ, "Z"); keyMap.put(R.id.keyX, "X"); keyMap.put(R.id.keyC, "C");
        keyMap.put(R.id.keyV, "V"); keyMap.put(R.id.keyB, "B"); keyMap.put(R.id.keyN, "N");
        keyMap.put(R.id.keyM, "M");
        
        keyMap.put(R.id.keySpace, "SPACE");
        keyMap.put(R.id.keyEnter, "ENTER");
        keyMap.put(R.id.keyBackspace, "BACKSPACE");

        // Use onTouch for proper press/release (ACTION_DOWN = press, ACTION_UP = release)
        for (Map.Entry<Integer, String> entry : keyMap.entrySet()) {
            findViewById(entry.getKey()).setOnTouchListener((v, event) -> {
                int action = event.getActionMasked();
                if (action == android.view.MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    if (isShiftPressed) {
                        keyboard.sendModifierAndKey((byte) 0x02, entry.getValue());
                        resetShift();
                    } else {
                        keyboard.sendKey(entry.getValue());
                    }
                } else if (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL) {
                    v.setPressed(false);
                    // Send key release (empty report)
                    keyboard.sendKeyRelease();
                }
                return true;
            });
        }

        findViewById(R.id.keyShift).setOnClickListener(v -> toggleShift());
        findViewById(R.id.keyNumbers).setOnClickListener(v -> keyboard.sendKey("TAB"));
    }


    private void toggleShift() {
        isShiftPressed = !isShiftPressed;
        Button btnShift = findViewById(R.id.keyShift);
        btnShift.setBackgroundColor(isShiftPressed ? 0xFF6200EE : 0xFF333333);
        statusText.setText(isShiftPressed ? "Shift: ON" : "Shift: OFF");
    }

    private void resetShift() {
        isShiftPressed = false;
        Button btnShift = findViewById(R.id.keyShift);
        btnShift.setBackgroundColor(0xFF333333);
    }

    private void startStatusMonitor() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HidController hid = HidController.get();
                if (hid != null) {
                    if (hid.isConnected()) {
                        statusText.setText("Connected: " + hid.getConnectedDeviceName());
                        statusText.setTextColor(0xFF00FF00); // Greenish
                    } else {
                        statusText.setText("Status: Not Connected\n(Pair from host device)");
                        statusText.setTextColor(0xFFAAAAAA);
                    }
                }
                statusHandler.postDelayed(this, 1500);
            }
        };
        statusHandler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statusHandler.removeCallbacksAndMessages(null);
    }
}
