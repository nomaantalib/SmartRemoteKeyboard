import socket
import pyautogui
import time

# To turn off the fail-safe, set to False (dangerous)
pyautogui.FAILSAFE = True

# Standard HID key mapping (simplified)
HID_KEY_MAP = {
    4: 'a', 5: 'b', 6: 'c', 7: 'd', 8: 'e', 9: 'f', 10: 'g', 11: 'h',
    12: 'i', 13: 'j', 14: 'k', 15: 'l', 16: 'm', 17: 'n', 18: 'o', 19: 'p',
    20: 'q', 21: 'r', 22: 's', 23: 't', 24: 'u', 25: 'v', 26: 'w', 27: 'x',
    28: 'y', 29: 'z', 30: '1', 31: '2', 32: '3', 33: '4', 34: '5', 35: '6',
    36: '7', 37: '8', 38: '9', 39: '0', 40: 'enter', 41: 'esc', 42: 'backspace',
    43: 'tab', 44: 'space', 45: '-', 46: '=', 47: '[', 48: ']', 49: '\\',
    51: ';', 52: "'", 53: '`', 54: ',', 55: '.', 56: '/', 57: 'capslock'
}

def handle_report(id, data):
    if id == 1: # Keyboard
        modifier = data[0]
        keycode = data[2]
        if keycode == 0: return # Key Release
        
        key = HID_KEY_MAP.get(keycode)
        if key:
            # Handle shift
            if modifier & 0x02 or modifier & 0x20:
                pyautogui.hotkey('shift', key)
            else:
                pyautogui.press(key)
                
    elif id == 2: # Mouse
        btns = data[0]
        dx = data[1]
        dy = data[2]
        wheel = data[3]
        
        # Relative move
        if dx != 0 or dy != 0:
            pyautogui.moveRel(dx, dy)
        
        # Scroll
        if wheel != 0:
            pyautogui.scroll(wheel * 10)
            
        # Clicks
        if btns & 1: pyautogui.click(button='left')
        elif btns & 2: pyautogui.click(button='right')

def start_server():
    UDP_IP = "0.0.0.0"
    UDP_PORT = 9999
    
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind((UDP_IP, UDP_PORT))
    
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)
    
    print(f"--- Smart Remote Wi-Fi Server ---")
    print(f"Server listening on {local_ip}:{UDP_PORT}")
    print(f"Enter this IP in your Android App.")
    print(f"Move your mouse to any corner of the screen to stop (Fail-safe).")
    
    while True:
        data, addr = sock.recvfrom(1024)
        try:
            msg = data.decode('utf-8')
            parts = msg.split(':')
            report_id = int(parts[0])
            report_data = [int(x) for x in parts[1].split(',')]
            handle_report(report_id, report_data)
        except Exception as e:
            pass

if __name__ == "__main__":
    start_server()
