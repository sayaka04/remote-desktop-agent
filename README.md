<br>
<br>
<p align="center">
<a href="https://github.com/sayaka04/remote-desktop-access"><img src="https://img.shields.io/badge/remote__desktop__agent-monorepo-0055ff?style=flat-pill" alt="remote-desktop-access" style="height:70px"></a>
</p>

<h3 align="center">A cross-platform, polling-based remote desktop visualization and command orchestration utility designed to traverse CGNAT over stateless HTTP.</h3>

<p align="center">
<a href="https://laravel.com/"><img src="https://img.shields.io/badge/Laravel-13-FF2D20?style=for-the-badge&logo=laravel&logoColor=white" alt="Laravel 13"></a>
<a href="https://react.dev/"><img src="https://img.shields.io/badge/React_/_Inertia-UI-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React & Inertia"></a>
<a href="https://openjfx.io/"><img src="https://img.shields.io/badge/JavaFX-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="JavaFX 21"></a>
<a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-Compose-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin & Jetpack Compose"></a>
</p>

This project is a cross-platform, polling-based remote desktop visualization and command orchestration utility structured as a monorepo. It allows an operator to view desktop frame snapshots and inject basic hardware-level interaction sequences (mouse movements, clicks, scrolling, and keystrokes) without relying on persistent connection daemons or direct incoming network routes.

---

## 1. Intended Use & Project Philosophy

This project is **not** designed to compete with, replace, or equal traditional remote desktop software such as RDP, VNC, TeamViewer, or WebRTC-based streaming tools. It does not attempt to achieve high frame rates, smooth visual rendering, or real-time operational efficiency.

Instead, it is an unconventional alternative engineered to solve a highly specific infrastructure problem for self-hosters faced with the following constraints:

- **The CGNAT Barrier:** In many regions, Internet Service Providers (ISPs) do not assign unique, publicly routable WAN IP addresses to residential subscribers. Instead, they utilize **Carrier-Grade NAT (CGNAT)**. This makes incoming port-forwarding structurally impossible, preventing traditional self-hosted remote access tools from establishing direct connections.
- **The Cost of WebSocket Alternatives:** While persistent bidirectional protocols (like WebSockets or WebRTC signaling servers) inherently bypass CGNAT by establishing outbound connections to a third party, they introduce financial and technical overhead. Maintaining open TCP socket states requires a dedicated Virtual Private Server (VPS), root terminal access to configure reverse proxies (such as Nginx), and persistent monthly hosting fees.
- **The Commodity Shared Hosting Solution:** This project rejects long-running socket daemons entirely. By operating over standard, stateless HTTP/HTTPS requests, **the orchestration server runs cleanly within standard commodity shared web hosting environments (such as cPanel)**. It requires no root privileges, no custom reverse proxy setups, and no background daemon management.

Ultimately, this is a niche project optimized for zero-configuration NAT traversal and minimal infrastructure costs, accepting lower performance and polling-based latency as a fundamental architectural tradeoff.

---

## 2. Technical Execution Loop

The system coordinates control between decoupled nodes utilizing a synchronous, outbound-only HTTP polling mechanism:

```
[ Operator Client ]                                 [ Central Server ]                         [ Host Execution Agent ]
 (Android / Web UI)                                  (Shared Hosting)                              (Behind CGNAT)
         │                                                   │                                            │
         │ 1. POST /commands/{uuid}/request                  │                                            │
         ├──────────────────────────────────────────────────>│                                            │
         │  (Queues JSON Action Sequence Array)              │                                            │
         │                                                   │ 2. GET /api/commands (Polling Loop)        │
         │                                                   │<───────────────────────────────────────────┤
         │                                                   │  (Fetches Pending Action Array)            │
         │                                                   │                                            │
         │                                                   │                                            │ (Executes sequences via AWT Robot)
         │                                                   │                                            │ (Captures active frame buffer)
         │                                                   │                                            │
         │                                                   │ 3. POST /commands/{uuid}/response          │
         │                                                   │<───────────────────────────────────────────┤
         │                                                   │  (Uploads Results & Multipart Bitmap)      │
         │ 4. GET /commands/{uuid} (Polling Loop)            │                                            │
         ├──────────────────────────────────────────────────>│                                            │
         │  (Retrieves New Frame Buffer & Status)            │                                            │
         V                                                   V                                            V

```

### Execution Loop Stages

1. **Command Batching:** The operator interacts with the remote viewport render via either the Android mobile client or the React Web UI. Captured actions are held locally in an **Action Composer Queue** and compiled into a single structured JSON payload array before being dispatched via a `POST /commands/{uuid}/request` request. This batching strategy prevents network congestion under a stateless HTTP configuration.
2. **Agent Outbound Polling:** The desktop agent on the target machine sits behind the CGNAT firewall and periodically queries the shared hosting database via an outbound HTTP `GET` request using a hardware validation token. It retrieves any open command payload assigned to its ID.
3. **Hardware Injection:** The agent extracts the JSON sequence array, scales the percentage-based bounding box coordinates to match the absolute native resolution of the host display, and uses the native Java Abstract Window Toolkit (`java.awt.Robot`) module to execute actual hardware-level operating system events.
4. **Display Serialization & Response:** Once the action array is processed, the agent takes a native screenshot, saves it to disk as a temporary binary asset, and sends it along with an execution log back to the server using a `POST /commands/{uuid}/response` multi-part form data boundary (`multipart/form-data`).
5. **Viewport Synchronization:** The operator client continuously polls the central server for updates. Once the server updates its database records and saves the new binary snapshot to its file storage, the client UI downloads the cache-busted image asset and updates the on-screen viewport to mirror the host machine's updated state.

### Traffic Minimization (Adaptive Backoff Protocol)

To ensure the polling loop does not exhaust shared hosting CPU limits or trigger automated account suspensions, both the agent and mobile clients operate an **Adaptive Backoff Protocol**:

- **Active Mode:** When an orchestration task is active, updates poll at a 1-second interval.
- **Idle Mode:** If no inputs are submitted within a configured inactivity timeout, the polling engine automatically throttles down to a conservative 5-second frequency, dropping idle server resource utilization by 80%.

---

## 3. Monorepo Repository Structure

The codebases are modular and split into separate sub-project directories:

```text
├── android-remote-client/   # Native Android Sender App (Kotlin / Jetpack Compose)
├── java-desktop-agent/      # Host Execution Worker (JavaFX 21 / java.awt.Robot)
├── laravel-server/          # Central Routing Engine & Web Interface (Laravel 13 / React / Inertia)
└── doc/                     # Platform Engineering Specifications & Core Manuals
    ├── API_DOCUMENTATION.md # REST API payloads, route declarations, and Sanctum integration
    ├── WEB_DOCUMENTATION.md # Stateful Web UI layouts, dashboards, and session management
    └── DEV_DOCUMENTATION.md # Environment setups, threading mechanics, and build pipelines

```

### Sub-Project Summary

- **`/laravel-server`:** The database core, API routing layer, and administrative web node. Built on **Laravel 13**, **React**, and **Inertia.js**, it manages system states and presents two operational interfaces: a fully authenticated Private Operator Dashboard for machine owners, and an unauthenticated, session-cookie-secured Public Client Portal (`/client/*`) that allows guest operators temporary control over a targeted machine using short-lived tokens.
- **`/java-desktop-agent`:** A lightweight cross-platform background execution loop built with **JavaFX 21** and managed via Gradle/Maven. It handles system tray minimizing, enforces a single-running instance via OS file-channel locks (`.remoteagent_app.lock`), processes the `java.awt.Robot` input operations, and uploads multi-part screen assets.
- **`/android-remote-client`:** A native Android sender mobile app developed with **Kotlin** and **Jetpack Compose** using an MVVM/UDF architecture pattern. It processes touch positions into percentage coordinates, utilizes Jetpack DataStore for persistent credential storage, and features hot-swappable Retrofit network definitions to switch target server URLs instantly.

---

## 4. Documentation Directory

**All granular technical design documentation and manual guides have been placed inside the root `/doc` folder.** Review these markdown references for specifics regarding schemas, protocols, or local compilation pipelines:

1. **[`/doc/API_DOCUMENTATION.md`](https://github.com/sayaka04/remote-desktop-agent/blob/main/doc/laravel-server/API_DOCUMENTATION.md):** Detailed references for every backend REST route. Outlines request/response body schemas, Laravel Sanctum bearer token handshakes, and strict API rate-limiting parameters.
2. **[`/doc/WEB_DOCUMENTATION.md`](https://github.com/sayaka04/remote-desktop-agent/blob/main/doc/laravel-server/WEB_DOCUMENTATION.md):** Specifications for the stateful web systems. Contains Inertia.js view sharing rules, public portal encrypted cookie variables, and token rotation hooks designed to invalidate temporary links instantly.
3. **[`/doc`](https://github.com/sayaka04/remote-desktop-agent/tree/main/doc):** Manuals for project developers. Documents minimum workspace environments, IDE configurations for IntelliJ IDEA and Android Studio, asynchronous execution rules (Kotlin Coroutines, StateFlow streams), and compiler steps for standalone binary packaging.

---

## 5. Deployment Core Requirements

To configure the project across your ecosystem, deploy the sub-components in the following sequence:

1. **Server Setup:** Upload the contents of `/laravel-server` to your hosting provider or local machine. Execute database structure scripts (`php artisan migrate`) to generate tables, and create structural asset shortcuts (`php artisan storage:link`).
2. **Agent Configuration:** Open `/java-desktop-agent` or deploy its artifact to your execution machine. Edit the properties file (`config.properties`) so the `api.base_url` parameter explicitly targets your backend domain URL.
3. **Client Configuration:** Compile `/android-remote-client` through Android Studio. Launch the application package on your device, navigate to settings to configure the base server route, and log in to begin remote control orchestration.
