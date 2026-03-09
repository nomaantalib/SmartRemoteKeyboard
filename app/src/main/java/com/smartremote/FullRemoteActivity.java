package com.smartremote;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smartremote.bluetooth.KeyboardManager;
import com.smartremote.bluetooth.MouseManager;
import com.smartremote.input.GestureProcessor;

public class FullRemoteActivity extends AppCompatActivity {

    private MouseManager mouse;
    private KeyboardManager keyboard;
    private GestureProcessor gestureProcessor;
    
    private GestureDetector gestureDetector;
    private float lastX, lastY;
    private boolean isScrolling = false;
    private EditText hiddenEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_remote);

        mouse = new MouseManager();
        keyboard = new KeyboardManager();
        gestureProcessor = new GestureProcessor();

        View touchSurface = findViewById(R.id.touchSurface);
        MaterialButton btnLeftClick = findViewById(R.id.btnLeftClick);
        MaterialButton btnRightClick = findViewById(R.id.btnRightClick);
        MaterialButton btnKeyboard = findViewById(R.id.btnKeyboard);
        ImageView btnBluetooth = findViewById(R.id.btnBluetooth);
        hiddenEditText = findViewById(R.id.hiddenEditText);

        gestureDetector = new GestureDetector(this, new GestureListener());

        touchSurface.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            int action = event.getActionMasked();
            
            if (event.getPointerCount() == 2) {
                isScrolling = true;
                if (action == MotionEvent.ACTION_MOVE) {
                    float dy = event.getY() - lastY;
                    float scrollVal = gestureProcessor.processScroll(dy);
                    if (scrollVal != 0) {
                        mouse.scroll(scrollVal);
                        lastY = event.getY();
                    }
                } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
                    lastY = event.getY();
                }
            } else if (event.getPointerCount() == 1) {
                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = event.getX();
                    lastY = event.getY();
                    isScrolling = false;
                } else if (action == MotionEvent.ACTION_MOVE && !isScrolling) {
                    float rawDx = event.getX() - lastX;
                    float rawDy = event.getY() - lastY;
                    
                    float[] processed = gestureProcessor.processMovement(rawDx, rawDy);
                    if (processed[0] != 0 || processed[1] != 0) {
                        mouse.moveCursor(processed[0], processed[1]);
                        lastX = event.getX();
                        lastY = event.getY();
                    }
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    mouse.holdLeft(false);
                }
            }
            return true;
        });


        btnLeftClick.setOnClickListener(v -> mouse.clickLeft());
        btnRightClick.setOnClickListener(v -> mouse.clickRight());
        btnBluetooth.setOnClickListener(v -> Toast.makeText(this, "Bluetooth Connection Menu", Toast.LENGTH_SHORT).show());

        btnKeyboard.setOnClickListener(v -> openKeyboard());

        setupHiddenEditText();
    }

    private void openKeyboard() {
        hiddenEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(hiddenEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void setupHiddenEditText() {
        hiddenEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    keyboard.sendKey("BACKSPACE");
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    keyboard.sendKey("ENTER");
                    return true;
                }
            }
            return false;
        });

        hiddenEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    String lastChar = s.toString().substring(s.length() - 1).toUpperCase();
                    keyboard.sendKey(lastChar);
                    
                    hiddenEditText.removeTextChangedListener(this);
                    hiddenEditText.setText("");
                    hiddenEditText.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mouse.clickLeft();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mouse.clickRight();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mouse.holdLeft(true);
        }
    }
}
