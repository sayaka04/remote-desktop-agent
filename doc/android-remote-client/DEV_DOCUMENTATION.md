# Android Remote Client Developer Documentation (`android-remote-client`)

## Overview

The **Android Remote Client** acts as the portable mobile sender within the Remote Command Orchestration ecosystem. Built exclusively using **Kotlin** and **Jetpack Compose**, the application enforces a strict **Model-View-ViewModel (MVVM)** architecture paired with **Unidirectional Data Flow (UDF)**.

Unlike traditional static applications, this client is designed for dynamic network routing, real-time bitmap rendering, and touch-to-coordinate translation, allowing operators to execute low-latency interaction payloads against remote operating systems via a central Laravel API layer.

---

## 🛠️ Local IDE Setup & Execution

Because this Android application is housed within a larger monorepo ecosystem, you must open the specific project directory in Android Studio to ensure the Gradle build system initializes correctly.

_(Note: If you are using Git sparse-checkout to pull only the Android folder, ensure your working directory is set appropriately)._

**1. Launch Android Studio**
Ensure you are running **Android Studio Jellyfish (2023.3.1)** or newer.

**2. Open the Specific Android Context**

- From the welcome screen, click **Open** (or `File > Open` if a project is already loaded).
- Navigate to the cloned repository.
- **⚠️ Important:** Do not select the root monorepo folder. You must explicitly select the **`android-remote-client`** folder.
- Click **OK**.

**3. Gradle Synchronization**
Once opened, Android Studio will automatically read the `build.gradle.kts` files and begin downloading necessary dependencies (Compose BOM, Retrofit, Coil, etc.). Wait for the Gradle sync process to complete and ensure there are no build errors.

**4. Connect a Target Device**

- **Physical Device:** Connect an Android phone (Android 7.1 / API 25 or higher) via USB or Wireless Debugging.
- **Emulator:** Alternatively, launch an Android Virtual Device (AVD) from the Device Manager.

**5. Build and Run**
Click the green **Run 'app'** play button in the top toolbar (or press `Shift + F10`). The IDE will compile the Kotlin code, build the APK, and deploy the application to your target device.

---

## 1. Directory & Package Architecture

The codebase follows a feature-by-feature package grouping strategy to ensure a clean separation of concerns between networking, local state, and interface layers:

```text
src/main/java/io/github/sayaka04/androidremoteclient/
├── MainActivity.kt                     # Single Activity Entry Point & Theme Wrapper
├── api/                                # Networking & Payload Layer
│   ├── ApiClient.kt                    # Singleton Retrofit Manager (Supports Hot-Swapping URLs)
│   ├── NetworkModels.kt                # GSON Data Classes (Requests, Responses, Action Payloads)
│   └── RemoteApiService.kt             # Retrofit REST Interface declarations
├── navigation/                         # Routing Layer
│   ├── AppNavigation.kt                # Compose NavHost & Backstack Management
│   └── Screen.kt                       # Sealed class for strongly-typed route definitions
├── ui/                                 # Presentation Layer (Feature Modules)
│   ├── auth/                           # Authentication, Server Configuration, & Login UI
│   ├── commands/                       # Active Session / Command selection UI
│   ├── devices/                        # Registered Hardware Node listing
│   ├── remote/                         # Core Orchestration Loop (The Remote Controller)
│   │   ├── components/                 # Viewport Sub-composables (Viewer, Composer Card, FAB)
│   │   ├── RemoteControlScreen.kt      # Main Execution Interface
│   │   ├── RemoteState.kt              # Stream UI & Targeting Properties
│   │   └── RemoteViewModel.kt          # Polling Engine & Payload Dispatcher
│   └── theme/                          # Custom Zinc/Shadcn Material 3 Color Palettes
└── util/
    └── PreferenceDatastoreUtil.kt      # Asynchronous local Key-Value Storage (DataStore)
```

---

## 2. Core Architectural Patterns

### 2.1 MVVM & Unidirectional Data Flow (UDF)

State management is handled natively via Kotlin Coroutines and `StateFlow`.

- **Immutable State:** Every UI screen observes a single `StateFlow` data class (e.g., `RemoteState`).
- **Event Forwarding:** Composables never mutate state directly. User interactions (clicks, text input, canvas gestures) dispatch events to the `ViewModel`.
- **State Emissions:** The `ViewModel` processes business logic (e.g., network calls) and emits a new, deeply-copied state object via `_state.update { ... }`, triggering highly optimized partial recompositions in the UI.

### 2.2 Dynamic API Routing & Interceptors

The application cannot rely on a hardcoded API endpoint. Operators may target different local or remote Laravel environments.

- **Hot-Swappable Retrofit:** `ApiClient.kt` tracks the current `baseUrl`. If the user updates the server route in the login settings, the active `Retrofit` instance is destroyed and lazily rebuilt upon the next request.
- **Bearer Injection:** An OkHttp Interceptor intercepts all outbound traffic, injecting `Authorization: Bearer <token>` seamlessly if a session token is present in memory.

### 2.3 Asynchronous Persistence (DataStore)

The legacy `SharedPreferences` API has been completely replaced by Android Jetpack **DataStore** (`PreferenceDatastoreUtil.kt`). All disk reads/writes (saving the `base_url` and `auth_token`) are suspended coroutines, preventing main-thread blocking and ensuring thread-safe transaction execution.

---

## 3. Remote Execution Loop Deep Dive

The `ui/remote/` module contains the most highly engineered components of the application, responsible for rendering the remote host's display and calculating interaction matrices.

### 3.1 Adaptive Polling Engine

To maintain a real-time display feed without exhausting mobile battery or saturating server bandwidth, `RemoteViewModel.kt` runs an infinite adaptive coroutine loop.

- **Active Phase (1000ms):** When the user is actively navigating or interacting, the client aggressively polls the server every 1 second, fetching the latest `HostData` and screenshot hashes.
- **Idle Backoff (5000ms):** If no UI interaction is detected for `60,000ms` (1 minute), the engine intentionally throttles network polling down to every 5 seconds. Any screen tap instantly revives the loop to the active phase.

### 3.2 Interactive Stream Viewer & Coordinate Translation

The `InteractiveStreamViewer` is a custom canvas powered by **Coil** for asynchronous image rendering and **Compose Gestures** for input tracking.

- **Cache Busting:** Coil image requests append a unique timestamp parameter to the URL to bypass the local cache, ensuring the latest frame is drawn.
- **Matrix Translation:** Using `detectTapGestures`, the raw pixel coordinates of the mobile screen touch are mathematically translated against the rendered image bounds. This converts the tap into a universal percentage value (`pctX`, `pctY`), which is accurate regardless of the mobile device's aspect ratio or current zoom scale.

### 3.3 The Action Composer Queue

Network latency is a significant hurdle in remote execution. Sending single clicks individually can lead to out-of-order execution or connection timeouts.
To solve this, the client uses a local queueing strategy:

1. **Stage:** Operators stage multiple `Action` sealed classes (e.g., `Action.Move`, `Action.Click(LEFT)`, `Action.Text("sudo update")`) via the `ActionComposerCard`.
2. **Translate:** Upon clicking execute, the UI models are serialized into `NetworkAction` JSON objects.
3. **Dispatch:** The entire array is wrapped in a `ClientPayload` and sent via a single HTTP `POST` request, optimizing the network trip and guaranteeing chronological execution on the host machine.

---

## 4. Theming & Interface Guidelines

The client actively rejects standard Android Material 3 dynamic coloring (which defaults to the user's wallpaper palette) in favor of a bespoke, professional administration aesthetic.

- **Shadcn/Zinc Palette:** Driven by `Theme.kt`, the application enforces a high-contrast monochrome gradient (`Zinc50` to `Zinc950`).
- **Edge-to-Edge Compositing:** The application leverages `WindowCompat.getInsetsController` to dynamically inject the app's background color directly into the native hardware Status Bar, creating a seamless, bezel-to-bezel viewport experience essential for the `RemoteControlScreen`.

---

## 5. Build & Compilation Strategy

Ensure your local IDE aligns with these project parameters:

- **Build System:** Gradle (Kotlin DSL / `build.gradle.kts`)
- **Language Level:** Kotlin 1.9+ (Official Code Style enabled via `gradle.properties`)
- **Min SDK:** `25` (Android 7.1) - Ensures >95% global device compatibility.
- **Target SDK:** `36` (Newer Android platform level) - Complies with the latest Google Play background and network security policies.
