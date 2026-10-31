# 📶 NetMapper - WiFi Heatmap & Network Toolkit

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg?style=for-the-badge&logo=kotlin)
![Android](https://img.shields.io/badge/Android-API%2024%2B-green.svg?style=for-the-badge&logo=android)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-orange.svg?style=for-the-badge&logo=jetpack-compose)
![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)
![Privacy](https://img.shields.io/badge/Privacy-100%25%20Offline-success.svg?style=for-the-badge)

**NetMapper** is a premium, 100% offline network utility app for Android. It turns your phone into a professional WiFi analysis tool, allowing you to map signal strength, find the best router channels, and discover all devices connected to your network.

No accounts. No cloud. Just pure network diagnostics.

---

## 🚀 Key Features

### 🗺️ WiFi Heatmap (Dead Zone Finder)
Walk around your house and create a visual map of your WiFi signal strength. 
*   A 10x10 interactive grid.
*   Tap a square to capture your current signal strength (RSSI).
*   The grid is color-coded: **Green (Strong)**, **Yellow (Medium)**, and **Red (Dead Zone)**.
*   Export your heatmap as an image (Rewarded Ad required).

### 📊 Channel Analyzer
Find out which WiFi channels are overcrowded in your area.
*   Scans all nearby WiFi networks.
*   Displays their signal strength and channel number.
*   Helps you optimize your router settings for maximum speed and less interference.

### 🕵️ Network Scanner (Who is on my WiFi?)
Discover every device connected to your local network.
*   Uses Kotlin Coroutines to rapidly ping your local IP range (e.g., 192.168.1.1 to 254).
*   Lists active IP addresses and hostnames.
*   Perfect for checking for unauthorized users or finding smart devices.

### 📡 Signal Finder
A real-time gauge showing your mobile network signal strength.
*   Uses `TelephonyManager` to display your 4G/5G signal in dBm.
*   Walk around your house to find the spot with the best cellular reception.

---

## 📖 How to Use NetMapper

### Heatmap Mode
1. Turn on your WiFi and connect to your network.
2. Open the **Heatmap** tab.
3. Stand in a spot in your room and tap the square on the grid that matches your location.
4. The app will record the signal strength. Move to a new spot and tap another square.
5. Watch the grid fill up to see exactly where your dead zones are.

### Network Scanner
1. Open the **Network Scanner** tab.
2. Tap the **Scan** button.
3. Wait a few seconds while the app pings your network.
4. A list of connected devices will appear.

---

## 🛠️ Tech Stack & Architecture

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM + Clean Architecture
*   **Networking:** `WifiManager` (for RSSI and scanning), `TelephonyManager` (for cellular signal), `NetworkInterface` & Sockets (for Ping Scanner).
*   **Asynchronous:** Kotlin Coroutines & Flow.
*   **Monetization:** Google AdMob (Banner & Rewarded) & UMP Consent.

---

## 🔐 Permissions Required

To function correctly, NetMapper requires the following Android permissions:
*   **ACCESS_FINE_LOCATION:** (Required by Android OS to read WiFi scan results and signal strength).
*   **ACCESS_WIFI_STATE & CHANGE_WIFI_STATE:** To trigger WiFi scans.
*   **ACCESS_NETWORK_STATE & INTERNET:** For AdMob and network pinging.

**We do NOT track your location.** The location permission is solely used by the Android OS to allow WiFi scanning.

---

## 🔒 Privacy Policy

NetMapper is built with privacy as a top priority:
1.  **100% Offline:** We do not collect or transmit any of your network data, IP addresses, or WiFi passwords.
2.  **Local Processing:** Everything runs on your device using standard Android APIs.
3.  **Ads:** We use Google AdMob, which may collect your device's advertising ID.

---

## 👨‍💻 Developer

**Mohd Arsh (CartoniQ)**
*   GitHub: [@CartonIQ](https://github.com/CartonIQ)
*   Android App Developer | Kotlin | Java | Termux 🚀

## ⚖️ License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
