package com.smartremote;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.smartremote.bluetooth.MouseManager;

public class TouchpadActivity extends AppCompatActivity {

    private MouseManager mouse;
    private GestureDetector gestureDetector;
    private float lastX, lastY;
    private boolean isScrolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchpad);

        mouse = new MouseManager();
        View touchSurface = findViewById(R.id.touchSurface);

        gestureDetector = new GestureDetector(this, new GestureListener());

        touchSurface.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            int action = event.getActionMasked();
            
            if (event.getPointerCount() == 2) {
                isScrolling = true;
                // Basic scroll logic for ultra demo
                if (action == MotionEvent.ACTION_MOVE) {
                    float dy = event.getY() - lastY;
                    mouse.moveCursor(0, dy * 0.5f); // simulate scroll Wheel
                }
            } else if (event.getPointerCount() == 1) {
                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = event.getX();
                    lastY = event.getY();
                    isScrolling = false;
                } else if (action == MotionEvent.ACTION_MOVE && !isScrolling) {
                    float dx = event.getX() - lastX;
                    float dy = event.getY() - lastY;
                    mouse.moveCursor(dx * 1.5f, dy * 1.5f);
                    lastX = event.getX();
                    lastY = event.getY();
                }
            }
            return true;
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
    }
}
