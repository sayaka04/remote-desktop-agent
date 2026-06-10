# Remote Command Orchestration — Desktop Agent (`java-desktop-agent`)

The **Java Desktop Agent** is a lightweight cross-platform background worker designed to execute remote input sequences on host machines. It securely connects to the central Laravel orchestration server, listens for incoming physical interaction arrays (mouse movements, clicks, keyboard sequences), and safely returns execution confirmations alongside native display captures.

Built using **JavaFX** and native OS bindings, the agent runs quietly in the system tray, utilizing an adaptive backoff loop to remain highly resource-efficient and transparent to the host computer.

---

## 🚀 Features

- 🎛️ **Background Process Execution:** Runs seamlessly in the system tray; closing the main window minimizes the application rather than killing it.
- 🤖 **Native OS Control:** Leverages `java.awt.Robot` to accurately inject mouse paths, scroll events, clicks, and text streams directly into the OS event layer.
- 📉 **Smart Adaptive Polling:** Automatically throttles down network resource use and server-side traffic when the machine is idle, instantly ramping back up to 1-second real-time responsiveness when a command is initiated.
- 📸 **Display Feedback Stream:** Generates automatic localized display buffer captures (`single-screen.png`) and securely transfers them back to the server using a multi-part boundary stream.
- 🔒 **Single-Instance Enforcement:** Uses a strict OS file-channel lock (`.remoteagent_app.lock`) in the user’s root home folder to guarantee that duplicate agent background processes never collide or double-consume server queue jobs.
- 🎭 **Modern Custom Theme:** Features a minimal, hardware-accelerated user interface featuring dark/light color schemes syncing with the main control panel aesthetic.

---

## 📦 Prerequisites

Before deploying the agent, verify your environment meets the minimum version constraints:

- **Operating System:** Windows 10/11, macOS, or modern Linux desktop distributions.
- **Java Runtime:** **Java SE 21** or greater (only required if running the raw `.jar` package; the native `.exe` distribution ships with an embedded, standalone runtime).

---

## 🛠️ Configuration (`config.properties`)

The application completely relies on a local configuration file named `config.properties`. This file **must reside in the exact same directory folder** as your executable binary (`.exe` or `.jar`).

### 1. Template Context

Create or edit the `config.properties` text layout using your preferred text editor:

```properties
# ==============================================================================
# REMOTE AGENT SYSTEM CONFIGURATION
# ==============================================================================

# --- Central API Connection Bounds
# The complete endpoint URL pointing directly to your Laravel environment routing plane
api.base_url=http://localhost:8000/api

# Your unique personal API authorization token generated via the Laravel Sanctum security screen
api.token=1|fnccfhDzYt3ivRavfkGj7W2RVjCsfnN8JcN56MaX39756c7a

# The operational identifier mapping this agent session directly to its dashboard link
api.command_id=019e4eb1-becc-722e-8db0-693386630d8d

# --- Adaptive Polling Adjustments (Seconds)
# Maximum time gap allowed to elapse before the system checks the queue while idle
poll.max_interval_sec=30

# Continuous inactivity safety margin (in seconds) before the adaptive backoff increments take effect
poll.timeout_sec=60

# --- Graphic Shell Buffers
# The hard maximum length constraint on scrolling logs displayed inside the UI viewer pane
log.max_lines=30

```

### 2. Live Adjustments

You do not need to reboot the host or shut down the process if you make edits to this configuration. Click **Options ➔ Reload Config** directly inside the agent interface to load your property changes into memory on the fly.

---

## 🖥️ Launching the Application

The Desktop Agent is distributed in two distinct pre-compiled distribution flavors depending on target host restrictions:

### Option A: Windows Native Executable (`.exe`)

Best for standalone deployments on Windows operating systems without managing localized Java paths or environment variables.

1. Place your customized `config.properties` file in your preferred execution location.
2. Double-click the prepackaged `remote-agent.exe` file.

### Option B: Cross-Platform Executable (`.jar`)

Best for launching across varying Unix environments, macOS workstations, or running straight from a terminal window.

1. Open your terminal shell and move directly into the target execution package context:

```bash
cd /path/to/your/agent-folder

```

2. Invoke the target executable binary utilizing your system’s active Java environment:

```bash
java -jar remote-agent.jar

```

---

## ⚙️ Operation & Interface Basics

Once launched, the agent initializes in the foreground, establishing initial data channel hooks to the server domain specified in your configurations.

```
       ┌─────────────────────────────────────────────────────────┐
       │ Options                                                 │
       ├─────────────────────────────────────────────────────────┤
       │  [ Start Process ]                                      │
       │                                                         │
       │  [2026-06-09 20:12:01] [INFO] | System ready...         │
       │  [2026-06-09 20:12:02] [INFO] | Restoring UI frame      │
       │                                                         │
       │                                                         │
       │                                                         │
       └─────────────────────────────────────────────────────────┘

```

### 1. Toggle Switch Control

- **Start Process:** Click the main button action labeled **Start Process** to set the worker thread loop alive. The label will switch text states to **Close Process**, and log feedback rows will stream indicators to verify real-time API monitoring metrics.
- **Close Process:** Click the button action again to safely pause polling worker arrays and place the thread completely at rest.

### 2. Window Control & System Tray Minimization

Clicking the standard **`[X]` close window control icon** in the window border header **will not stop the application**. The agent overrides this default window callback to seamlessly shift active windows out of sight into the OS target system tray bar.

- To pull the active visual viewport interface frame back into context: locate the display computer icon (`pc1.png`) inside your OS system task tray context, right-click, and select **Open**.

### 3. Context Menu Actions

Right-clicking the taskbar background tray layout offers immediate command routes:

- **Open:** Restores and forces focus on the primary user logs panel.
- **Hide:** Explicitly sends the visual window layout out of focus back to the tray background loop.
- **Exit:** Completely terminates background workers, deletes the runtime file system instance lock handler (`.remoteagent_app.lock`), and cleanly exits the application process.

---

## 🛠️ Troubleshooting

### 🛑 "Another instance of Remote Agent is already running. Exiting..."

- **Cause:** The application detected an active instance lock structure. This happens if an instance of the agent is already hidden out of view inside the system tray background routines or if a previous session closed unexpectedly without executing a clean tracking resource release step.
- **Resolution:** Open your operating system Task Manager or process monitor tool, look up `java` or `remote-agent`, terminate the lingering process execution tree, and clean out the hidden `.remoteagent_app.lock` placeholder file sitting directly inside your home root profile directory (`C:\Users\<YourUsername>\` on Windows) if it remains.

### ⚠️ "Connection Error (Server Offline?)"

- **Cause:** The agent is unable to poll endpoints matching your configuration values or is getting blocked by localized perimeter security software.
- **Resolution:** Open `config.properties` and verify your `api.base_url` syntax formatting string matches perfectly (including the final trailing `/api` route). Ensure target hosts have open routing clearance to target outbound port connections.

---

> ℹ️ _For developers seeking information regarding deep core structures, system architecture charts, package layers, or styling code parameters, consult the codebase's separate developer documentation file **`DEV_DOCUMENTATION.md`**._
