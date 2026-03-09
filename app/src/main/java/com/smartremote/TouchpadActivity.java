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
                if (action == MotionEvent.ACTION_MOVE) {
                    float dy = event.getY() - lastY;
                    // Apply scroll dampening to feel like a real trackpad
                    if (Math.abs(dy) > 2) { 
                        mouse.scroll(dy * 0.2f); 
                        lastY = event.getY();
                    }
                } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
                    lastY = event.getY(); // Reset scroll anchor
                }
            } else if (event.getPointerCount() == 1) {
                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = event.getX();
                    lastY = event.getY();
                    isScrolling = false;
                } else if (action == MotionEvent.ACTION_MOVE && !isScrolling) {
                    float dx = event.getX() - lastX;
                    float dy = event.getY() - lastY;
                    
                    // Filter out unnoticeable micro-jitter noise
                    if (Math.abs(dx) > 0.5f || Math.abs(dy) > 0.5f) {
                        // Apply dynamic acceleration (mouse ballistics)
                        float speedMultiplier = 1.2f + (Math.max(Math.abs(dx), Math.abs(dy)) * 0.05f);
                        float finalDx = dx * speedMultiplier;
                        float finalDy = dy * speedMultiplier;
                        
                        mouse.moveCursor(finalDx, finalDy);
                        
                        lastX = event.getX();
                        lastY = event.getY();
                    }
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    mouse.holdLeft(false); // Stop any active drag
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
            // Can trigger a right-click on double tap
            mouse.clickRight();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Click-and-drag capability on long press
            mouse.holdLeft(true);
        }
    }
}
