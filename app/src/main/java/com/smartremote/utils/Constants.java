package com.smartremote.utils;

public class Constants {
    // Network
    public static final int SERVER_PORT = 5050;
    public static final int DISCOVERY_PORT = 5051;
    public static final String DISCOVERY_MAGIC = "SMARTREMOTE_DISCOVER";
    public static final String DISCOVERY_RESPONSE = "SMARTREMOTE_SERVER";
    
    // Packet Event Types (Binary Protocol)
    public static final byte EVENT_KEY_PRESS = 1;
    public static final byte EVENT_KEY_RELEASE = 2;
    public static final byte EVENT_MOUSE_MOVE = 3;
    public static final byte EVENT_MOUSE_CLICK = 4;
    public static final byte EVENT_SCROLL = 5;
    public static final byte EVENT_MEDIA = 6;
    
    // Mouse Buttons
    public static final byte MOUSE_LEFT = 1;
    public static final byte MOUSE_RIGHT = 2;
    public static final byte MOUSE_MIDDLE = 4;
    
    // Media Keys (custom codes for our protocol)
    public static final byte MEDIA_PLAY_PAUSE = 1;
    public static final byte MEDIA_NEXT = 2;
    public static final byte MEDIA_PREV = 3;
    public static final byte MEDIA_VOL_UP = 4;
    public static final byte MEDIA_VOL_DOWN = 5;
    public static final byte MEDIA_MUTE = 6;
    public static final byte MEDIA_BRIGHTNESS_UP = 7;
    public static final byte MEDIA_BRIGHTNESS_DOWN = 8;
    
    // Touchpad Settings
    public static final float TOUCHPAD_SENSITIVITY = 1.2f;
    public static final float TOUCHPAD_ACCELERATION = 0.05f;
    public static final float SCROLL_DAMPENING = 0.2f;
    public static final float JITTER_THRESHOLD = 0.5f;
    public static final int CLICK_DURATION_MS = 15;
}
