package com.smartremote;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.smartremote.bluetooth.KeyboardManager;

public class KeyboardActivity extends AppCompatActivity {

    private KeyboardManager keyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);

        keyboard = new KeyboardManager();

        Button btnA = findViewById(R.id.keyA);
        Button btnB = findViewById(R.id.keyB);
        Button btnC = findViewById(R.id.keyC);
        Button btnSpace = findViewById(R.id.keySpace);

        btnA.setOnClickListener(v -> keyboard.sendKey("A"));
        btnB.setOnClickListener(v -> keyboard.sendKey("B"));
        btnC.setOnClickListener(v -> keyboard.sendKey("C"));
        btnSpace.setOnClickListener(v -> keyboard.sendKey("SPACE"));
    }
}
