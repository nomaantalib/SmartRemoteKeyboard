package com.smartremote.bluetooth;

public class HidDescriptor {
    // Defines that we are both a Keyboard and a Mouse
    public static final byte[] KEYBOARD_MOUSE_REPORT_MAP = {
        // --- KEYBOARD ---
        (byte) 0x05, (byte) 0x01, // Usage Page (Generic Desktop)
        (byte) 0x09, (byte) 0x06, // Usage (Keyboard)
        (byte) 0xA1, (byte) 0x01, // Collection (Application)
        (byte) 0x85, (byte) 0x01, //   Report ID (1)
        (byte) 0x05, (byte) 0x07, //   Usage Page (Key Codes)
        (byte) 0x19, (byte) 0xE0, //   Usage Minimum (224)
        (byte) 0x29, (byte) 0xE7, //   Usage Maximum (231)
        (byte) 0x15, (byte) 0x00, //   Logical Minimum (0)
        (byte) 0x25, (byte) 0x01, //   Logical Maximum (1)
        (byte) 0x75, (byte) 0x01, //   Report Size (1)
        (byte) 0x95, (byte) 0x08, //   Report Count (8)
        (byte) 0x81, (byte) 0x02, //   Input (Data, Variable, Absolute) - Modifier byte
        (byte) 0x95, (byte) 0x01, //   Report Count (1)
        (byte) 0x75, (byte) 0x08, //   Report Size (8)
        (byte) 0x81, (byte) 0x01, //   Input (Constant) - Reserved byte
        (byte) 0x95, (byte) 0x06, //   Report Count (6)
        (byte) 0x75, (byte) 0x08, //   Report Size (8)
        (byte) 0x15, (byte) 0x00, //   Logical Minimum (0)
        (byte) 0x25, (byte) 0x65, //   Logical Maximum (101)
        (byte) 0x05, (byte) 0x07, //   Usage Page (Key codes)
        (byte) 0x19, (byte) 0x00, //   Usage Minimum (0)
        (byte) 0x29, (byte) 0x65, //   Usage Maximum (101)
        (byte) 0x81, (byte) 0x00, //   Input (Data, Array) - Key arrays (6 bytes)
        (byte) 0xC0,              // End Collection

        // --- MOUSE ---
        (byte) 0x05, (byte) 0x01, // Usage Page (Generic Desktop)
        (byte) 0x09, (byte) 0x02, // Usage (Mouse)
        (byte) 0xA1, (byte) 0x01, // Collection (Application)
        (byte) 0x85, (byte) 0x02, //   Report ID (2)
        (byte) 0x09, (byte) 0x01, //   Usage (Pointer)
        (byte) 0xA1, (byte) 0x00, //   Collection (Physical)
        (byte) 0x05, (byte) 0x09, //     Usage Page (Button)
        (byte) 0x19, (byte) 0x01, //     Usage Minimum (1)
        (byte) 0x29, (byte) 0x03, //     Usage Maximum (3)
        (byte) 0x15, (byte) 0x00, //     Logical Minimum (0)
        (byte) 0x25, (byte) 0x01, //     Logical Maximum (1)
        (byte) 0x95, (byte) 0x03, //     Report Count (3)
        (byte) 0x75, (byte) 0x01, //     Report Size (1)
        (byte) 0x81, (byte) 0x02, //     Input (Data, Variable, Absolute) - 3 button bits
        (byte) 0x95, (byte) 0x01, //     Report Count (1)
        (byte) 0x75, (byte) 0x05, //     Report Size (5)
        (byte) 0x81, (byte) 0x01, //     Input (Constant) - 5 bit padding
        (byte) 0x05, (byte) 0x01, //     Usage Page (Generic Desktop)
        (byte) 0x09, (byte) 0x30, //     Usage (X)
        (byte) 0x09, (byte) 0x31, //     Usage (Y)
        (byte) 0x09, (byte) 0x38, //     Usage (Wheel)
        (byte) 0x15, (byte) 0x81, //     Logical Minimum (-127)
        (byte) 0x25, (byte) 0x7F, //     Logical Maximum (127)
        (byte) 0x75, (byte) 0x08, //     Report Size (8)
        (byte) 0x95, (byte) 0x03, //     Report Count (3)
        (byte) 0x81, (byte) 0x06, //     Input (Data, Variable, Relative) - X, Y, Wheel (3 bytes)
        (byte) 0xC0,              //   End Collection
        (byte) 0xC0               // End Collection
    };
}
