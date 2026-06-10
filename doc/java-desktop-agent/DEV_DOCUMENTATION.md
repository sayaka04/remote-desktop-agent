# Remote Agent Developer Documentation (`java-desktop-agent`)

## Overview

The Java Desktop Agent is the physical execution host within the Remote Command Orchestration ecosystem. Built with **Java (JavaFX 21)**, it operates as a high-performance cross-platform background service that periodically polls the central Laravel server for queued command payloads. Once a payload is received, the agent translates JSON directives into hardware-level input events using the native `java.awt.Robot` architecture, captures localized desktop frames, and safely transmits results back to the server.

---

## 1. Directory & Package Architecture

The codebase follows a clean, single-responsibility Maven/Gradle-style layout separating underlying network protocols, system interaction managers, and user interface layouts:

```text
src/main/
├── java/
│   └── remoteagent/
│       ├── Launcher.java                 # Execution Entry point & Single Instance Lock
│       ├── Main.java                     # JavaFX Application Context Startup
│       ├── api/
│       │   ├── ApiClient.java            # Stateless HTTP/Java 11 Client Wrapper
│       │   ├── ApiController.java        # Domain Payload Fetching & Submission Services
│       │   └── MultipartBodyBuilder.java # Raw Multipart/Form-Data Binary Assembler
│       ├── controller/
│       │   └── WindowController.java     # FXML Interface Interaction Bridge
│       ├── handler/
│       │   ├── ActionHandler.java        # Low-Level java.awt.Robot OS Event Injector
│       │   ├── JobHandler.java           # Asynchronous Polling/Worker Loop Thread
│       │   └── PollingTimer.java         # Smart Adaptive Backoff State Machine
│       └── utils/
│               ├── Config.java           # Thread-Safe config.properties Manager
│               ├── Json.java             # Google Gson Abstraction Engine
│               └── LogUtil.java          # Contextual Timestamp Logger Formatter
└── resources/
    ├── pc1.png                           # Main System Tray & Taskbar Icon Resource
    └── remoteagent/
        ├── css/
        │   ├── styles.css                # Layout Dimensions and Control Overrides
        │   ├── theme-dark.css            # Dark Mode Core Palette Accent Variables
        │   └── theme-light.css           # Light Mode Core Palette Accent Variables
        └── fxml/
            └── Window.fxml               # Semantic UI Structure Mapping View Definition

```

---

## 2. Core Lifecycle & Bootstrapping

### 2.1 Instance Protection Entry Point (`Launcher.java`)

The application entry point is decoupled from the JavaFX `Application` class to explicitly bypass strict modular runtime validation rules and enforce a strict instance lock.

- **Single Instance Enforcement:** Upon launch, `main()` invokes `checkAndLockInstance()`. This attempts to establish an exclusive file-channel lock (`FileLock`) over a hidden state file named `.remoteagent_app.lock` located directly within the host user's root home folder (`System.getProperty("user.home")`).
- **Lock Lifecycle:** If another instance holds the channel lock, the method returns `false`, prints an error, and terminates execution immediately. If successful, it registers a JVM `ShutdownHook` thread to cleanly release the channel, close file handles, and purge the lock file when the agent process stops normally.
- **Modular Runtime Bypass:** Once verified, `Launcher` invokes `Main.main(args)`, smoothly entering the JavaFX execution pipeline.

### 2.2 Framework Initialization (`Main.java`)

`Main` extends `javafx.application.Application` and instantiates primary architectural controllers.

- **Implicit Termination Suppression:** The framework overrides default exit triggers by executing `Platform.setImplicitExit(false)`. This ensures that closing the primary user interface stage does not terminate background loop threads or drop system tray interactions.
- **Component Bonding:** Instantiates the primary `JobHandler` worker, hands it over to the `WindowManager` stage controller, and configures the `TrayManager` background service with decoupled `Runnable` handles for switching window visibility scopes on demand.

---

## 3. Threading & Adaptive Polling Engine

```
 [User clicks "Start Process"] ──> Spawns Worker Thread (JobHandler)
                                           │
                                           ▼
                               ┌───────────────────────┐
                               │ ApiController.get()   │ <───┐
                               └───────────────────────┘     │
                                           │                 │
                        ┌──────────────────┴─────────────────┐│
                        ▼ (Commands Present?)                ▼│
               ┌─────────────────┐                  ┌──────────────────┐
               │    YES (Active) │                  │     NO (Idle)    │
               └─────────────────┘                  └──────────────────┘
                        │                                    │
                        ▼                                    ▼
           Maps actions to Robot class              Calculates backoff delta
           Takes screenshot descriptor              Increments sleep interval
                        │                                    │
                        ▼                                    ▼
           ApiController.respond()                  PollingTimer.sleep()
                        │                                    │
                        └──────────────────┬─────────────────┘
                                           │
                                           ▼
                                 Loop evaluates isRunning

```

### 3.1 Loop Management (`JobHandler.java`)

All network polling and OS interactions occur inside an isolated, dedicated background `Thread` context. Clicking **"Start Process"** toggles `isRunning = true` and kicks off the run loop.

1. **State Dispatching:** The loop continuously contacts the server endpoint via `ApiController.requestData()`.
2. **Path A (Active Payload Execution):** If the server return array contains structural actions, the handler drops the state string to `"Active"`, walks the JSON array, and pipes elements straight to the `ActionHandler` engine. It immediately forces an outbound `apiController.respond()` post including a fresh display capture, and triggers `pollingTimer.reset()` to snap the loop interval back to high-responsiveness mode.
3. **Path B (Inactivity Backoff):** If the command payload returns empty, the state reverts to `"Idle"`, and `pollingTimer.update()` calculates the sleep padding.
4. **Safety Error Bounds:** A comprehensive `try-catch` wrapper encircles the connection step. If the server drops offline or structural exceptions are thrown, the engine intercepts the fault, writes a warnings array to the UI log, prevents application crashes, and enters the idle backoff routine to minimize host stress.

### 3.2 Smart Backoff Engine (`PollingTimer.java`)

To optimize network overhead and server load, the polling mechanism utilizes a dynamic backoff strategy governed by a state machine:

- **Default Interval:** Holds at a tight **1-second** frequency during continuous server action queues or immediate initial idling phases.
- **Inactivity Margin:** Tracks time elapsed using `System.currentTimeMillis() - lastActiveTime`. If this duration surpasses the configured threshold (e.g., `poll.timeout_sec=60`), the system expands the poll sleep step by **+2 seconds** on each empty cycle.
- **Capping Bounds:** The expanding delay increments until it encounters the safety ceiling configured by `poll.max_interval_sec` (e.g., 30 seconds), where it rests until new command payloads wake the agent back up.

---

## 4. Hardware Interaction Layer (`ActionHandler.java`)

The hardware integration layer uses a thread-safe, non-instantiable dispatcher that translates structural JSON command blocks into direct OS desktop actions via `java.awt.Robot`:

### 4.1 Input Mapping Vector Matrix

The incoming JSON actions are parsed and processed through a clean switch-case matrix:

- **`move_mouse`:** Extracts `x` and `y` coordinate integers and repositions the cursor across the display plane via `robot.mouseMove(x, y)`.
- **`click` / `double_click`:** Resolves target click buttons (`left`, `right`, `middle`), binds the correct `InputEvent` mask, and triggers synchronized press/release actions. Double-clicks repeat the execution sequence with a localized minimal pause.
- **`scroll`:** Extracts directional magnitude weights and issues native vertical scrolling events via `robot.mouseWheel(amount)`.
- **`type_text`:** Implements string simulation. To reliably type special characters without layout bugs, it copies the target text payload onto the host system `Clipboard`, then commands the `Robot` to programmatically trigger standard keyboard paste shortcuts (`Ctrl + V` on Windows/Linux or `Cmd + V` on macOS).

### 4.2 Display Snapshot Engine

Executes `takeScreenshot()` immediately following action queues. It calculates the full spatial geometry of primary display layouts via `Toolkit.getDefaultToolkit().getScreenSize()`, captures the raw pixel buffer via `robot.createScreenCapture()`, and flushes a compressed artifact file (`single-screen.png`) to the local folder using `ImageIO.write()`.

---

## 5. Network & Payload Plane

### 5.1 Native Transceiver (`ApiClient.java`)

The application avoids bulky external network libraries by relying entirely on the native Java 11 `java.net.http.HttpClient` package configured for fully synchronous delivery paths.

- **Header Integrity:** Automatically structures outbound headers on every network frame, passing an explicit token signature (`Authorization: Bearer <token>`) alongside standard JSON and Multi-part boundary declarations.

### 5.2 Multipart Engine (`MultipartBodyBuilder.java`)

Because native Java 11 HTTP tools lack a built-in multi-part form-data stream assembler, the agent features a custom raw byte payload factory:

- **Boundary Generation:** Establishes a unique boundary tag utilizing epoch timestamps (`Boundary-` + `System.currentTimeMillis()`).
- **Stream Packaging:** Serializes string keys using standard text dispositions and appends raw image binary blocks (`byte[]`) enclosed within explicit boundary wrappers, matching the multi-part requirements of Laravel server API filters.

### 5.3 Domain Integrator (`ApiController.java`)

Maps the raw `ApiClient` transport channels to specific web controller paths:

- **Data Pulls:** Polls the server route `/commands/{uuid}` using a plain `GET` request. It transforms the returned JSON string into a structured `JsonObject` via the internal `Json` parsing utility and returns the inner data array to the worker loop.
- **Data Push confirmations:** Encapsulates localized execution confirmations inside a multi-part form layout (`host_payload[success]`, `host_payload[message]`), appends the raw image array retrieved from `single-screen.png`, and streams the entire package to `/commands/{uuid}/response` via a `POST` request.

---

## 6. User Interface & Presentation Layer

### 6.1 Context Architecture (`WindowManager.java` & `TrayManager.java`)

The UI layout is managed via decoupled view controllers that balance user interface visibility with seamless background execution:

- **`WindowManager`:** Pulls semantic structure from `Window.fxml`, extracts the underlying `WindowController` link, binds the primary system stage scene context, attaches the embedded desktop icon (`pc1.png`), and intercepts stage close queries (`setOnCloseRequest`) to minimize the app instead of killing it.
- **`TrayManager`:** Handles integration with the OS native system taskbar tray via `java.awt.SystemTray`. It configures a localized popup menu schema featuring explicit actions (**Open**, **Hide**, **Exit**) wrapped in asynchronous JavaFX thread dispatches via `Platform.runLater()`.

### 6.2 View Presentation (`WindowController.java`)

Provides live visualization of the agent's background loops and network states:

- **Log Buffer Optimization:** Appends formatted rows safely from background threads using `Platform.runLater()`. To prevent memory leaks during long-running background sessions, the controller constantly monitors the visual log line length via `logArea.getParagraphs().size()`. If the count surpasses the configured threshold (`log.max_lines`), it truncates the oldest log entry at the top, maintaining a performant user interface.
- **Hot Configuration Reloading:** Maps the UI menu click `handleReloadConfig()` to flush current property arrays out of memory and invoke `Config.reload()`. This allows developers and system administrators to dynamically adjust limits like log visibility ranges and polling loops without rebooting the application.

---

## 7. Local Development & IDE Setup

If you wish to modify the agent, implement new OS action models, or customize the UI layout, the codebase is fully optimized for quick import into modern Java IDEs. **IntelliJ IDEA** is highly recommended for working with the JavaFX/Gradle pipeline.

### Prerequisites

- **Java Development Kit (JDK):** Version 21 or higher
- **Build System:** Gradle (The project includes an integrated wrapper script).
- **IDE:** IntelliJ IDEA (Community edition).

### Importing and Running the Project (IntelliJ IDEA)

1. **Open the Project:** Launch IntelliJ, choose **File ➔ Open**, and select the root `java-desktop-agent` directory (the folder containing the `build.gradle.kts` file).
2. **Gradle Sync:** Allow IntelliJ to read the Gradle configuration script. It will automatically pull the correct OS-specific JavaFX 21 platform binaries (`win`, `mac`, or `linux`) along with the Google Gson libraries from Maven Central.
3. **Project Structure Verification:** Open **File ➔ Project Structure** (`Ctrl+Alt+Shift+S`). Ensure your Project SDK is explicitly targeted to a valid installation of JDK 21+ and that the project language level matches.
4. **Target Entry Launcher Configuration:**

- ⚠️ **Do not attempt to run `Main.java` directly.** Because this codebase intentionally avoids strict JavaFX runtime module pathways to simplify standalone compilation, executing `Main` directly will fail with a `"JavaFX runtime components are missing"` initialization error.
- Instead, locate the launcher proxy class file: `src/main/java/remoteagent/Launcher.java`.

5. **Run the Application:** Right-click the `Launcher.java` file in the project navigation tree and click **Run 'Launcher.main()'**. IntelliJ will build your internal code models, attach asset resources, and spin up the interface.
6. **Local Debug Configuration Location:** When launching directly from an IDE editor context, the working directory maps to the project root folder. Ensure a valid `config.properties` file is placed in that root directory (alongside `src/` and `build.gradle.kts`) so the `Config` management class can find it during bootstrap verification loops.
