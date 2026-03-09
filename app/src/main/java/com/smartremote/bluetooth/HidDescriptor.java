package com.smartremote.bluetooth;

public class HidDescriptor {
    // Defines that we are both a Keyboard and a Mouse
    public static final byte[] KEYBOARD_MOUSE_REPORT_MAP = {
        // --- KEYBOARD ---
        0x05, 0x01,                    // Usage Page (Generic Desktop)
        0x09, 0x06,                    // Usage (Keyboard)
        (byte) 0xA1, 0x01,             // Collection (Application)
        (byte) 0x85, 0x01,             //   Report ID (1)
        0x05, 0x07,                    //   Usage Page (Key Codes)
        0x19, (byte) 0xE0,             //   Usage Minimum (224)
        0x29, (byte) 0xE7,             //   Usage Maximum (231)
        0x15, 0x00,                    //   Logical Minimum (0)
        0x25, 0x01,                    //   Logical Maximum (1)
        0x75, 0x01,                    //   Report Size (1)
        (byte) 0x95, 0x08,             //   Report Count (8)
        (byte) 0x81, 0x02,             //   Input (Data, Variable, Absolute) - Modifier byte
        (byte) 0x95, 0x01,             //   Report Count (1)
        0x75, 0x08,                    //   Report Size (8)
        (byte) 0x81, 0x01,             //   Input (Constant) - Reserved byte
        (byte) 0x95, 0x06,             //   Report Count (6)
        0x75, 0x08,                    //   Report Size (8)
        0x15, 0x00,                    //   Logical Minimum (0)
        0x25, 0x65,                    //   Logical Maximum (101)
        0x05, 0x07,                    //   Usage Page (Key codes)
        0x19, 0x00,                    //   Usage Minimum (0)
        0x29, 0x65,                    //   Usage Maximum (101)
        (byte) 0x81, 0x00,             //   Input (Data, Array) - Key arrays (6 bytes)
        (byte) 0xC0,                    // End Collection

        // --- MOUSE ---
        0x05, 0x01,                    // Usage Page (Generic Desktop)
        0x09, 0x02,                    // Usage (Mouse)
        (byte) 0xA1, 0x01,             // Collection (Application)
        (byte) 0x85, 0x02,             //   Report ID (2)
        0x09, 0x01,                    //   Usage (Pointer)
        (byte) 0xA1, 0x00,             //   Collection (Physical)
        0x05, 0x09,                    //     Usage Page (Buttons)
        0x19, 0x01,                    //     Usage Minimum (1)
        0x29, 0x03,                    //     Usage Maximum (3)
        0x15, 0x00,                    //     Logical Minimum (0)
        0x25, 0x01,                    //     Logical Maximum (1)
        (byte) 0x95, 0x03,             //     Report Count (3)
        0x75, 0x01,                    //     Report Size (1)
        (byte) 0x81, 0x02,             //     Input (Data, Variable, Absolute)
        (byte) 0x95, 0x01,             //     Report Count (1)
        0x75, 0x05,                    //     Report Size (5)
        (byte) 0x81, 0x03,             //     Input (Constant)
        0x05, 0x01,                    //     Usage Page (Generic Desktop)
        0x09, 0x30,                    //     Usage (X)
        0x09, 0x31,                    //     Usage (Y)
        0x09, 0x38,                    //     Usage (Wheel)
        0x15, (byte) 0x81,             //     Logical Minimum (-127)
        0x25, 0x7F,                    //     Logical Maximum (127)
        0x75, 0x08,                    //     Report Size (8)
        (byte) 0x95, 0x03,             //     Report Count (3)
        (byte) 0x81, 0x06,             //     Input (Data, Variable, Relative)
        (byte) 0xC0,                    //   End Collection
        (byte) 0xC0                     // End Collection
    };
}
