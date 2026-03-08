package com.smartremote.input;

import java.util.HashMap;

public class KeyMapper {

    static HashMap<String,Byte> map = new HashMap<>();

    static {
        // Letters
        map.put("A", (byte)0x04); map.put("B", (byte)0x05); map.put("C", (byte)0x06);
        map.put("D", (byte)0x07); map.put("E", (byte)0x08); map.put("F", (byte)0x09);
        map.put("G", (byte)0x0A); map.put("H", (byte)0x0B); map.put("I", (byte)0x0C);
        map.put("J", (byte)0x0D); map.put("K", (byte)0x0E); map.put("L", (byte)0x0F);
        map.put("M", (byte)0x10); map.put("N", (byte)0x11); map.put("O", (byte)0x12);
        map.put("P", (byte)0x13); map.put("Q", (byte)0x14); map.put("R", (byte)0x15);
        map.put("S", (byte)0x16); map.put("T", (byte)0x17); map.put("U", (byte)0x18);
        map.put("V", (byte)0x19); map.put("W", (byte)0x1A); map.put("X", (byte)0x1B);
        map.put("Y", (byte)0x1C); map.put("Z", (byte)0x1D);

        // Numbers
        map.put("1", (byte)0x1E); map.put("2", (byte)0x1F); map.put("3", (byte)0x20);
        map.put("4", (byte)0x21); map.put("5", (byte)0x22); map.put("6", (byte)0x23);
        map.put("7", (byte)0x24); map.put("8", (byte)0x25); map.put("9", (byte)0x26);
        map.put("0", (byte)0x27);

        // Special keys
        map.put("ENTER", (byte)0x28);
        map.put("ESCAPE", (byte)0x29);
        map.put("BACKSPACE", (byte)0x2A);
        map.put("TAB", (byte)0x2B);
        map.put("SPACE", (byte)0x2C);
        map.put(" ", (byte)0x2C);
        map.put("-", (byte)0x2D);
        map.put("=", (byte)0x2E);
        map.put("[", (byte)0x2F);
        map.put("]", (byte)0x30);
        map.put("\\", (byte)0x31);
        map.put(";", (byte)0x33);
        map.put("'", (byte)0x34);
        map.put("`", (byte)0x35);
        map.put(",", (byte)0x36);
        map.put(".", (byte)0x37);
        map.put("/", (byte)0x38);
    }

    public static byte getKeyCode(String key) {
        Byte code = map.get(key.toUpperCase());
        return code != null ? code : 0x00;
    }
}
