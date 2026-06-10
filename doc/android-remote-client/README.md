# Remote Command Orchestration - Android Client (`android-remote-client`)

This directory houses the **`android-remote-client`** component of the Remote Command Orchestration platform. Built exclusively with **Kotlin** and **Jetpack Compose**, this native mobile application serves as the portable, on-the-go orchestration interface for operators to control remote desktop hosts.

---

## 🎯 Purpose & Monorepo Context

Within the broader monorepo ecosystem, this Android application acts as a dedicated **Remote Sender**.

While the _Laravel Server_ acts as the central hub and the _Java Desktop Agent_ acts as the physical execution receiver, this Android client empowers administrators to securely log in from anywhere, visualize incoming desktop display frames in real-time, and dispatch precisely calculated physical interaction arrays (mouse movements, clicks, scrolling, and keyboard macros) back to the server.

---

## 🚀 Key Features

- 📱 **100% Native Jetpack Compose:** Built entirely with modern declarative UI paradigms, featuring a heavily customized Material 3 theme inspired by the high-contrast, professional Shadcn/Zinc aesthetic.
- 📡 **Dynamic Target Networking:** Hardcoding API URLs is a thing of the past. Operators can easily update the target Laravel orchestration server URL on the fly via the Login Settings tab. The internal Retrofit client intelligently rebuilds itself and manages Bearer token injection automatically.
- 🖼️ **Interactive Stream Viewer:** Features a responsive, edge-to-edge canvas that renders incoming localized desktop snapshots. Utilizing intelligent coordinate translation, taps on the mobile screen are mathematically converted into exact percentage-based coordinates (`pctX`, `pctY`) relative to the target host's display ratio.
- 📥 **Action Composer Queue:** To mitigate mobile network latency and prevent out-of-order execution, actions are not sent individually. Instead, operators batch movements, multi-button clicks, and text injections into a local queue before executing a single, unified POST request.
- ⚡ **Adaptive Polling Engine:** Preserves mobile battery and server bandwidth. The internal coroutine engine idles at a 5-second polling interval but aggressively ramps up to 1-second ticks the moment operator interaction is detected.
- 🔐 **Persistent Encrypted Sessions:** Leverages modern Android Jetpack `PreferenceDataStore` for asynchronous, non-blocking persistence of authorization tokens and server routing preferences.

---

## 🛠️ Prerequisites & Requirements

To compile, build, and deploy this application, ensure your local development environment meets the following baseline requirements:

- **IDE:** Android Studio Panda 3 (2025.3.3) or newer.
- **Language:** Kotlin 2+
- **Minimum SDK:** API Level 25 (Android 7.1 Nougat)
- **Target SDK:** API Level 36 (Newer Android platform level)
- **Dependencies:** An active internet connection for initial Gradle synchronization (pulling libraries like Retrofit, OkHttp, Coil, and Compose BOM).

---

## ⚙️ Getting Started & Installation

1. **Open the Project Context:** Launch Android Studio and select **Open**. Navigate to the root monorepo directory and select the `android-remote-client` folder.
2. **Gradle Synchronization:** Allow Android Studio to index the project and sync the `build.gradle.kts` files. Ensure all Compose and network dependencies are resolved successfully.
3. **Connect a Device / Emulator:** Connect a physical Android device via USB debugging or start an Android Virtual Device (AVD).
4. **Build & Run:** Click the **Run 'app'** button (or press `Shift + F10`). The APK will compile and launch on your target device.

---

## 🎮 Operational Workflow

Once the application is running on your device, follow these steps to establish a remote link:

1. **Configure the Server Route:** - Upon launching, you will be greeted by the Login Screen.
   - Swipe or tap to the **Settings** tab.
   - Enter the full URL to your hosted Laravel server's API layer (e.g., `http://192.168.1.100:8000/api/` or `https://yourdomain.com/api/`). _Note: The trailing `/api/` is required._
   - Tap **Save Configuration**.
2. **Authenticate:**
   - Return to the **Login** tab.
   - Input your registered Operator Email and Password.
   - Upon successful authentication, your token is secured in the local DataStore, and you are routed to the Dashboard.
3. **Select a Target:**
   - The **Devices Screen** lists all registered hardware nodes. Select an online device.
   - The **Commands Screen** lists active authorization vectors (Commands/Sessions) for that device. Select the active command to enter the Remote Viewport.
4. **Execute Actions:**
   - Tap anywhere on the live stream to drop a targeted coordinate marker (Move Action).
   - Tap the Floating Action Button (FAB) in the bottom right to open the **Action Composer Card**.
   - Stack additional actions like Clicks (Left/Right) or Text Injections.
   - Hit **Execute Sequence** to dispatch the payload.

---

## 🛑 Troubleshooting

### ⚠️ "Connection Error (Login Failed)"

- **Cause:** The mobile application cannot resolve the target API Base URL, or the Laravel server is rejecting the connection.
- **Resolution:** Double-check the URL in the Settings tab. Ensure you have included the `http://` or `https://` prefix. If testing locally, ensure your mobile device (or emulator) is connected to the same local network subnet as your development machine hosting the server. Use your machine's IPv4 address instead of `localhost`.

### 🖼️ "Black Screen / Endless Loading Stream"

- **Cause:** The target Java Desktop Agent is not actively uploading screenshot buffers to the server, or the command UUID has expired.
- **Resolution:** Verify that the Java Desktop Agent is running and connected on the physical host machine. Press the **Refresh** button on the top navigation bar of the Android client to force a manual fetch.

---

> ℹ️ _For deep technical insights regarding the MVVM architecture, state management rules (`StateFlow`), routing parameters, and API network models, please consult the internal **`DEV_DOCUMENTATION.md`** file located in this same directory._
