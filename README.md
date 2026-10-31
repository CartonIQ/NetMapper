# NetMapper - Map, Scan, and Master your network.
Package `com.cartoniq.netmapper` | Kotlin 2.0.21 | AGP 8.7.3 | Gradle 8.9 | Compose Material 3 | minSdk 24 / target 34

## Build (ARM Termux)
1. `pkg install openjdk-17 gradle aapt2` and install the Android SDK (platform 34, build-tools 34.0.0).
2. Create `local.properties`:  `sdk.dir=/path/to/android-sdk`
3. In `gradle.properties` uncomment `android.aapt2FromMavenOverride=...` and point it at your ARM aapt2
   (Maven's aapt2 is x86 and will not run on ARM).
4. Generate the wrapper jar once (it cannot be bundled offline):
   `gradle wrapper --gradle-version 8.9`
5. `./gradlew assembleDebug`  ->  `app/build/outputs/apk/debug/app-debug.apk`

## Tools
- **WiFi Heatmap**: 10x10 grid, tap a cell to record live RSSI (green / yellow / red). Export shares a dBm grid (rewarded ad).
- **Channel Analyzer**: scans nearby networks, bar chart, channel numbers, best 2.4 GHz channel suggestion.
- **Network Scanner**: finds devices on your /24 with coroutines (rewarded ad). Tap a device to scan its ports.
- **Signal Finder**: 4G/5G dBm gauge, carrier, bars and live history.
- **Extras**: Wi-Fi Details (SSID, link speed, gateway, DNS) and Ping & Port Scanner (no root needed).

## Ads
Google TEST IDs only. UMP consent runs on start. If no rewarded ad can load (offline/no fill) the action runs anyway.
Replace the IDs in `AdManager.kt` and `AndroidManifest.xml` before release.
