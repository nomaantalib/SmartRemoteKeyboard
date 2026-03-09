import socket
import struct
import ctypes
import time

# Options
PORT = 5050
HOST = "0.0.0.0"

# CTypes for high performance Windows API
# 0x01 = MOVE, 0x02 = LEFTDOWN, 0x04 = LEFTUP, 0x08 = RIGHTDOWN, 0x10 = RIGHTUP
# 0x0800 = WHEEL
MOUSEEVENTF_MOVE = 0x0001
MOUSEEVENTF_LEFTDOWN = 0x0002
MOUSEEVENTF_LEFTUP = 0x0004
MOUSEEVENTF_RIGHTDOWN = 0x0008
MOUSEEVENTF_RIGHTUP = 0x0010
MOUSEEVENTF_WHEEL = 0x0800

def move_mouse(dx, dy):
    # ctypes.windll.user32.mouse_event(flags, dx, dy, data, extraInfo)
    ctypes.windll.user32.mouse_event(MOUSEEVENTF_MOVE, int(dx), int(dy), 0, 0)

def scroll_mouse(dy):
    ctypes.windll.user32.mouse_event(MOUSEEVENTF_WHEEL, 0, 0, int(dy) * 120, 0)

def press_key(vk_code):
    ctypes.windll.user32.keybd_event(vk_code, 0, 0, 0)

def release_key(vk_code):
    ctypes.windll.user32.keybd_event(vk_code, 0, 2, 0)

# Map HID Usage (USB) to Windows Virtual Key (VK)
HID_TO_VK = {
    4: 0x41, 5: 0x42, 6: 0x43, 7: 0x44, 8: 0x45, 9: 0x46, 10: 0x47, 11: 0x48,
    12: 0x49, 13: 0x4A, 14: 0x4B, 15: 0x4C, 16: 0x4D, 17: 0x4E, 18: 0x4F, 19: 0x50,
    20: 0x51, 21: 0x52, 22: 0x53, 23: 0x54, 24: 0x55, 25: 0x56, 26: 0x57, 27: 0x58,
    28: 0x59, 29: 0x5A, 30: 0x31, 31: 0x32, 32: 0x33, 33: 0x34, 34: 0x35, 35: 0x36,
    36: 0x37, 37: 0x38, 38: 0x39, 39: 0x30, 40: 0x0D, 41: 0x1B, 42: 0x08, 43: 0x09,
    44: 0x20, 45: 0xBD, 46: 0xBB, 47: 0xDB, 48: 0xDD, 49: 0xDC, 51: 0xBA, 52: 0xDE,
    53: 0xC0, 54: 0xBC, 55: 0xBE, 56: 0xBF, 57: 0x14
}

def start_server():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind((HOST, PORT))
    server.listen(1)
    
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)

    print(f"--- Smart Remote High-Perf Server (TCP) ---")
    print(f"Listening on {local_ip}:{PORT}")
    print(f"Waiting for connection...")

    while True:
        conn, addr = server.accept()
        print(f"Connected by {addr}")
        try:
            while True:
                data = conn.recv(4)
                if not data:
                    break
                
                # event, key, val1, val2
                event, key, val1, val2 = struct.unpack("BBBB", data)
                
                # Conversion for signed bytes (B to b)
                # If value is > 127 it's negative
                def to_signed(b):
                    return b if b < 128 else b - 256

                if event == 1: # Key Press
                    vk = HID_TO_VK.get(key)
                    if vk: press_key(vk)
                elif event == 2: # Key Release
                    vk = HID_TO_VK.get(key)
                    if vk: release_key(vk)
                elif event == 3: # Mouse Move
                    move_mouse(to_signed(key), to_signed(val1))
                elif event == 4: # Mouse Click
                    # val1 is button mask
                    if val1 == 1: # Left
                        ctypes.windll.user32.mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0)
                        time.sleep(0.01)
                        ctypes.windll.user32.mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0)
                    elif val1 == 2: # Right
                        ctypes.windll.user32.mouse_event(MOUSEEVENTF_RIGHTDOWN, 0, 0, 0, 0)
                        time.sleep(0.01)
                        ctypes.windll.user32.mouse_event(MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0)
                elif event == 5: # Scroll
                    scroll_mouse(to_signed(key))
                    
        except Exception as e:
            print(f"Connection lost: {e}")
        finally:
            conn.close()

if __name__ == "__main__":
    start_server()
