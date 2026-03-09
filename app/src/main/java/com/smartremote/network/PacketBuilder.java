package com.smartremote.network;

public class PacketBuilder {
    // 1 = key press, 2 = key release, 3 = mouse move, 4 = mouse click, 5 = scroll
    public static final byte TYPE_KEY_PRESS = 1;
    public static final byte TYPE_KEY_RELEASE = 2;
    public static final byte TYPE_MOUSE_MOVE = 3;
    public static final byte TYPE_MOUSE_CLICK = 4;
    public static final byte TYPE_SCROLL = 5;

    public static byte[] keyPress(byte keycode) {
        return new byte[]{TYPE_KEY_PRESS, keycode, 0, 0};
    }

    public static byte[] keyRelease(byte keycode) {
        return new byte[]{TYPE_KEY_RELEASE, keycode, 0, 0};
    }

    public static byte[] mouseMove(int dx, int dy) {
        return new byte[]{
                TYPE_MOUSE_MOVE,
                (byte) dx,
                (byte) dy,
                0
        };
    }

    public static byte[] mouseClick(byte buttons) {
        return new byte[]{TYPE_MOUSE_CLICK, buttons, 0, 0};
    }

    public static byte[] scroll(int dy) {
        return new byte[]{TYPE_SCROLL, (byte) dy, 0, 0};
    }
}
