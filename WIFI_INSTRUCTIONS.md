# How to Use Wi-Fi Fallback Mode

If your phone or PC does not support Bluetooth HID, you can use the Wi-Fi fallback.

### 1. Requirements on PC:
- Install Python: [python.org](https://www.python.org/downloads/)
- Install `pyautogui`:
  ```bash
  pip install pyautogui
  ```

### 2. Setup the Server:
1. Copy `wifi_server.py` to your PC.
2. Run the server:
   ```bash
   python wifi_server.py
   ```
3. Look for the **IP Address** shown in the terminal (e.g., `192.168.1.5`).

### 3. Setup the Android App:
1. Open the **Smart Remote** app on your phone.
2. At the bottom, enter the **IP Address** you saw in the terminal.
3. Toggle the **Wi-Fi Fallback** switch to **ON**.
4. Now, any keyboard or touchpad activity will be sent over your local Wi-Fi.

### Troubleshooting:
- Ensure both the phone and PC are on the **same Wi-Fi network**.
- Check your PC's **Firewall** settings – allow Python to use TCP port `5050`.
- If your mouse moves to a corner, the Python script will stop as a fail-safe.
