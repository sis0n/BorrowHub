# ADB Command Cheat Sheet

This document provides a quick reference for commonly used `adb` (Android Debug Bridge) commands, which are particularly helpful during the development and testing of BorrowHub.

## Essential Commands

### Port Forwarding (Important for Local Backend)
Forwards the Android emulator's port `8000` to the host machine's port `8000`. **This is crucial** for allowing the Android app to communicate with a local Laravel backend running on `localhost:8000`.
```bash
adb reverse tcp:8000 tcp:8000
```
*(Run this every time you restart your emulator or adb server).*

---

### Devices
Lists all connected devices and running emulators.
```bash
adb devices
```

### Logcat (System Logs)
Prints the device log to the console. Very useful for debugging crashes and inspecting runtime behavior.
```bash
adb logcat
```

Filter by a specific tag (e.g., `BorrowHub`):
```bash
adb logcat -s BorrowHub
```

Filter for network logs (OkHttp):
```bash
adb logcat *:S OkHttp:D -v color
```

Clear the logcat buffer:
```bash
adb logcat -c
```

### Application Management
Install an APK file to the device:
```bash
adb install path/to/your/app.apk
```

Uninstall an application using its package name:
```bash
adb uninstall com.example.borrowhub
```

Clear application data and cache (acts like a fresh install):
```bash
adb shell pm clear com.example.borrowhub
```

### Server Management
If ADB becomes unresponsive or devices aren't showing up, restart the server.
```bash
adb kill-server
adb start-server
```

### Interactive Shell
Open an interactive shell on the device/emulator to run direct Linux commands:
```bash
adb shell
```