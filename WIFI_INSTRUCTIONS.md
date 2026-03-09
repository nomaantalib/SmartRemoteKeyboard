# Smart Remote - Wi-Fi / USB Setup Guide

## Requirements on PC
- Python 3.8+ → [python.org](https://www.python.org/downloads/)
- No extra libraries needed (uses only built-in modules)

## Quick Start

### 1. Run the Server on your PC
```bash
python wifi_server.py
```
You will see:
```
╔══════════════════════════════════════════════╗
║   Smart Remote - High Performance Server     ║
║  IP Address : 192.168.1.5                    ║
║  TCP Port   : 5050                           ║
║  Auto-Detect: ON (UDP 5051)                  ║
║  Media Keys : Supported                      ║
╚══════════════════════════════════════════════╝
```

### 2. Connect from Android App

**Option A: Auto-Detect (Recommended)**
1. Open Smart Remote on your phone.
2. Toggle **Wi-Fi Fallback** switch ON (leave IP field empty).
3. The app will auto-detect the server on your network!

**Option B: Manual IP**
1. Enter the IP address shown in the server terminal.
2. Toggle Wi-Fi switch ON.

### 3. Start Using!
- Open **Keyboard Mode**, **Touchpad Pro**, or **Full Remote**.
- Everything works instantly over Wi-Fi.

---

## USB Cable Mode (Ultra Low Latency)

For **~1ms latency** (faster than Wi-Fi):

1. Connect phone to PC via USB cable.
2. Enable **USB Tethering** on Android (Settings → Connections → Tethering).
3. In the app, enter IP: `192.168.42.129` (or check your PC's USB network).
4. Toggle Wi-Fi switch ON.

---

## Supported Features over Wi-Fi

| Feature | Status |
|---------|--------|
| Keyboard | ✅ Full A-Z, numbers, special keys |
| Touchpad | ✅ Movement, scroll, click, drag |
| Left/Right Click | ✅ |
| Scroll | ✅ Two-finger |
| Media Keys | ✅ Play/Pause, Volume, Next/Prev |
| Auto-Detect | ✅ No IP needed |
| Bluetooth HID | ✅ Works simultaneously |

---

## Troubleshooting

- **"No server found"**: Ensure both devices are on the **same Wi-Fi network**.
- **Firewall**: Allow Python through Windows Firewall on TCP port `5050` and UDP port `5051`.
- **Antivirus**: Some antivirus software blocks socket connections. Whitelist `wifi_server.py`.
- **USB Mode**: If USB tethering IP doesn't work, try `192.168.42.1` instead.
