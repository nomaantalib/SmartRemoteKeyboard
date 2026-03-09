import socket
import struct
import ctypes
import time
import threading

# ===== Configuration =====
PORT = 5050
DISCOVERY_PORT = 5051
HOST = "0.0.0.0"
DISCOVERY_MAGIC = "SMARTREMOTE_DISCOVER"
DISCOVERY_RESPONSE = "SMARTREMOTE_SERVER"

# ===== Windows Input Constants =====
MOUSEEVENTF_MOVE = 0x0001
MOUSEEVENTF_LEFTDOWN = 0x0002
MOUSEEVENTF_LEFTUP = 0x0004
MOUSEEVENTF_RIGHTDOWN = 0x0008
MOUSEEVENTF_RIGHTUP = 0x0010
MOUSEEVENTF_WHEEL = 0x0800

# Virtual Key codes for media
VK_MEDIA_PLAY_PAUSE = 0xB3
VK_MEDIA_NEXT_TRACK = 0xB0
VK_MEDIA_PREV_TRACK = 0xB1
VK_VOLUME_UP = 0xAF
VK_VOLUME_DOWN = 0xAE
VK_VOLUME_MUTE = 0xAD

# ===== Input Functions =====
def move_mouse(dx, dy):
    ctypes.windll.user32.mouse_event(MOUSEEVENTF_MOVE, int(dx), int(dy), 0, 0)

def scroll_mouse(dy):
    ctypes.windll.user32.mouse_event(MOUSEEVENTF_WHEEL, 0, 0, int(dy) * 120, 0)

def press_key(vk_code):
    ctypes.windll.user32.keybd_event(vk_code, 0, 0, 0)

def release_key(vk_code):
    ctypes.windll.user32.keybd_event(vk_code, 0, 2, 0)

def tap_key(vk_code):
    press_key(vk_code)
    time.sleep(0.01)
    release_key(vk_code)

# ===== HID Usage to Windows VK Map =====
HID_TO_VK = {
    4: 0x41, 5: 0x42, 6: 0x43, 7: 0x44, 8: 0x45, 9: 0x46, 10: 0x47, 11: 0x48,
    12: 0x49, 13: 0x4A, 14: 0x4B, 15: 0x4C, 16: 0x4D, 17: 0x4E, 18: 0x4F, 19: 0x50,
    20: 0x51, 21: 0x52, 22: 0x53, 23: 0x54, 24: 0x55, 25: 0x56, 26: 0x57, 27: 0x58,
    28: 0x59, 29: 0x5A,
    # Numbers
    30: 0x31, 31: 0x32, 32: 0x33, 33: 0x34, 34: 0x35, 35: 0x36,
    36: 0x37, 37: 0x38, 38: 0x39, 39: 0x30,
    # Special
    40: 0x0D, 41: 0x1B, 42: 0x08, 43: 0x09, 44: 0x20,
    45: 0xBD, 46: 0xBB, 47: 0xDB, 48: 0xDD, 49: 0xDC,
    51: 0xBA, 52: 0xDE, 53: 0xC0, 54: 0xBC, 55: 0xBE, 56: 0xBF, 57: 0x14
}

# ===== Media Key Map =====
MEDIA_MAP = {
    1: VK_MEDIA_PLAY_PAUSE,
    2: VK_MEDIA_NEXT_TRACK,
    3: VK_MEDIA_PREV_TRACK,
    4: VK_VOLUME_UP,
    5: VK_VOLUME_DOWN,
    6: VK_VOLUME_MUTE,
    # 7, 8 = Brightness (handled differently on each system)
}

def to_signed(b):
    """Convert unsigned byte to signed (-128 to 127)"""
    return b if b < 128 else b - 256

# ===== Auto-Discovery Thread =====
def discovery_server():
    """Listens for UDP broadcast from the Android app and responds."""
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind(("0.0.0.0", DISCOVERY_PORT))
    
    hostname = socket.gethostname()
    print(f"[Discovery] Listening for auto-detect on UDP port {DISCOVERY_PORT}")
    
    while True:
        try:
            data, addr = sock.recvfrom(1024)
            message = data.decode('utf-8').strip()
            if message == DISCOVERY_MAGIC:
                response = f"{DISCOVERY_RESPONSE}:{hostname}"
                sock.sendto(response.encode(), addr)
                print(f"[Discovery] Responded to {addr[0]} with hostname '{hostname}'")
        except Exception as e:
            print(f"[Discovery] Error: {e}")

# ===== Main TCP Server =====
def start_server():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((HOST, PORT))
    server.listen(1)
    
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)

    print(f"")
    print(f"╔══════════════════════════════════════════════╗")
    print(f"║   Smart Remote - High Performance Server     ║")
    print(f"╠══════════════════════════════════════════════╣")
    print(f"║  IP Address : {local_ip:<30} ║")
    print(f"║  TCP Port   : {PORT:<30} ║")
    print(f"║  Hostname   : {hostname:<30} ║")
    print(f"╠══════════════════════════════════════════════╣")
    print(f"║  Auto-Detect: ON (UDP {DISCOVERY_PORT})                  ║")
    print(f"║  Media Keys : Supported                     ║")
    print(f"╚══════════════════════════════════════════════╝")
    print(f"")
    print(f"Waiting for connection from Android app...")

    while True:
        conn, addr = server.accept()
        print(f"\n✓ Connected: {addr[0]}:{addr[1]}")
        try:
            while True:
                data = conn.recv(4)
                if not data or len(data) < 4:
                    break
                
                event, key, val1, val2 = struct.unpack("BBBB", data)

                if event == 1:  # Key Press
                    vk = HID_TO_VK.get(key)
                    if vk:
                        press_key(vk)

                elif event == 2:  # Key Release
                    if key == 0:
                        # Release all: iterate and release any pressed keys
                        pass  # No-op on release-all
                    else:
                        vk = HID_TO_VK.get(key)
                        if vk:
                            release_key(vk)

                elif event == 3:  # Mouse Move
                    dx = to_signed(key)
                    dy = to_signed(val1)
                    if dx != 0 or dy != 0:
                        move_mouse(dx, dy)

                elif event == 4:  # Mouse Click
                    btn = key  # button mask
                    if btn == 1:
                        ctypes.windll.user32.mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0)
                        time.sleep(0.01)
                        ctypes.windll.user32.mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0)
                    elif btn == 2:
                        ctypes.windll.user32.mouse_event(MOUSEEVENTF_RIGHTDOWN, 0, 0, 0, 0)
                        time.sleep(0.01)
                        ctypes.windll.user32.mouse_event(MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0)

                elif event == 5:  # Scroll
                    scroll_val = to_signed(key)
                    if scroll_val != 0:
                        scroll_mouse(scroll_val)

                elif event == 6:  # Media
                    media_vk = MEDIA_MAP.get(key)
                    if media_vk:
                        tap_key(media_vk)

        except ConnectionResetError:
            print(f"✗ Connection reset by {addr[0]}")
        except Exception as e:
            print(f"✗ Error: {e}")
        finally:
            conn.close()
            print(f"  Waiting for reconnection...")


if __name__ == "__main__":
    # Start discovery thread in background
    discovery_thread = threading.Thread(target=discovery_server, daemon=True)
    discovery_thread.start()
    
    # Start main TCP server
    start_server()
