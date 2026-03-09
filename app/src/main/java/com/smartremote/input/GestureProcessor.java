package com.smartremote.input;

import com.smartremote.utils.Constants;

/**
 * Advanced gesture processing for touchpad input.
 * Handles acceleration, jitter filtering, and scroll dampening.
 */
public class GestureProcessor {
    
    private float sensitivity;
    private float acceleration;
    private float scrollDampening;
    private float jitterThreshold;
    
    public GestureProcessor() {
        this.sensitivity = Constants.TOUCHPAD_SENSITIVITY;
        this.acceleration = Constants.TOUCHPAD_ACCELERATION;
        this.scrollDampening = Constants.SCROLL_DAMPENING;
        this.jitterThreshold = Constants.JITTER_THRESHOLD;
    }
    
    /**
     * Apply mouse ballistics (acceleration curve) to raw dx/dy.
     * Slow movements = precise, fast movements = large jumps.
     */
    public float[] processMovement(float rawDx, float rawDy) {
        // Filter micro-jitter noise
        if (Math.abs(rawDx) < jitterThreshold && Math.abs(rawDy) < jitterThreshold) {
            return new float[]{0, 0};
        }
        
        // Dynamic speed multiplier based on distance
        float speed = (float) Math.sqrt(rawDx * rawDx + rawDy * rawDy);
        float multiplier = sensitivity + (speed * acceleration);
        
        // Clamp multiplier to prevent cursor from flying off screen
        multiplier = Math.min(multiplier, 5.0f);
        
        return new float[]{rawDx * multiplier, rawDy * multiplier};
    }
    
    /**
     * Process 2-finger scroll gesture.
     * Returns dampened scroll value that feels natural.
     */
    public float processScroll(float rawDy) {
        // Apply dampening and threshold
        if (Math.abs(rawDy) < 2.0f) {
            return 0;
        }
        return rawDy * scrollDampening;
    }
    
    /**
     * Detect if a tap is a "click" vs start of a drag.
     * Returns true if the total movement is below the click threshold.
     */
    public boolean isClickGesture(float totalDx, float totalDy) {
        float distance = (float) Math.sqrt(totalDx * totalDx + totalDy * totalDy);
        return distance < 10.0f; // 10px threshold
    }
    
    // Setters for user-configurable sensitivity
    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }
    
    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }
    
    public void setScrollDampening(float dampening) {
        this.scrollDampening = dampening;
    }
}
