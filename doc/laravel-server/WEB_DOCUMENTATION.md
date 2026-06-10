# Remote Command Orchestration Web UI & Client Portal Reference

## Overview

The Web Interface serves a dual purpose: providing an administration portal for device owners (**Private User Portal**) and an unauthenticated, token-validated gateway for guest operators (**Public Client Portal**).

Unlike the REST API which utilizes stateless bearer tokens, the web ecosystem relies on stateful Laravel cookies and session storage.

---

## 1. Authentication & State Strategy

### 1.1 Private User UI

- **Mechanism:** Standard stateful session authentication (`web` guard).
- **Access:** Restricted to registered operators. Managed via Laravel Fortify / Breeze / Jetstream.
- **Architecture:** Renders state-driven Inertia.js views combined with React components.

### 1.2 Public Client Portal

- **Mechanism:** Stateful Session-based Access Validation.
- **Access:** Unauthenticated users possessing a valid Command `UUID` and `Access Token`.
- **Session Lifecycle:** Upon validation, the server persists credentials within the client's encrypted session cookie. Subsequent interactions read directly from this session context rather than passing structural auth headers.
- `session('client_uuid')` -> Matches command identifier.
- `session('client_token')` -> Matches generated secure string (`access_token`).

---

## 2. Rate Limiting Matrix

The web routing engine applies targeted throttling bands depending on the execution context to mitigate brute-force vector threats and prevent layout exhaustion.

| Context / Tier       | Limit         | Scope                                                            | Focus                                         |
| -------------------- | ------------- | ---------------------------------------------------------------- | --------------------------------------------- |
| **Public Gateway**   | 30 req / min  | `GET /client`, `POST /client/logout`                             | Browsing & entry views                        |
| **Portal Auth Gate** | 100 req / min | `POST /client/authenticate`                                      | Token verification                            |
| **Guest Control**    | 120 req / min | `POST /client/commands/{uuid}/payload`, `GET /client/.../status` | High-frequency mouse/keyboard sequence relays |
| **Private Operator** | 100 req / min | `GET /dashboard`, `/devices/*`, `/commands/*`                    | Regular management                            |
| **Private Control**  | 200 req / min | `POST /commands/{uuid}/payload`                                  | Low-latency dashboard control                 |

---

## 3. Public Client Portal (`/client/*`)

### 3.1 Portal Base View

Loads the entry dashboard. If the client has active valid token credentials stored inside their current session, the execution console is served; otherwise, an authentication challenge screen is visible.

- **Endpoint:** `GET /client`
- **Access:** Public (Throttled 30/min)
- **Response:** Inertia Render (`client/index`)

```json
// View properties passed to the Client Index Component
{
  "activeCommand": {
    "uuid": "018f3c5d-ee33-8abd-93bc-3456789012ef",
    "name": "Emergency Live Stream Connection",
    "screenshot_path": "screenshots/1/2/20260609_180000_abc123.jpg",
    "has_client_request": false,
    "has_host_response": true,
    "updated_at": "2026-06-09T19:22:00.000000Z",
    "device": {
      "uuid": "018f3b4c-da22-7fca-82ab-1234567890cd",
      "name": "Main Office Workstation"
    }
  }
}
```

### 3.2 Portal Authentication Challenge

Submits connection credentials to authorize a temporary web session. Successful validation flashes the keys to the active session payload.

- **Endpoint:** `POST /client/authenticate`
- **Content-Type:** `application/json`

**Request Body**

```json
{
  "uuid": "018f3c5d-ee33-8abd-93bc-3456789012ef",
  "token": "zY9X8w7V6u5T4s3R2q1P..."
}
```

**Response** (`302 Redirect`)
Redirects back to `/client` with valid session state parameters.

### 3.3 Transmit Control Sequence Payload

Transmits a block of sequential input operations to execute on the targeted host agent. Optimized for high execution throughput; bypasses structural Inertia layout updates by using direct JSON messaging.

- **Endpoint:** `POST /client/commands/{uuid}/payload`
- **Access:** Authenticated Session Token match required.

**Request Body**

```json
{
  "payload": [
    { "type": "move_mouse", "x": 1920, "y": 1080 },
    { "type": "click", "button": "left" },
    { "type": "type_text", "text": "systemctl restart docker" },
    { "type": "key_stroke", "key": "enter", "modifiers": ["ctrl", "alt"] },
    { "type": "scroll", "axis": "vertical", "amount": -5 }
  ]
}
```

**Response** (`200 OK`)

```json
{
  "success": true,
  "message": "Command sequence transmitted."
}
```

### 3.4 Poll Host Status Updates

Background endpoint used by front-end clients to pull current framing status and refresh targeted container snapshots.

- **Endpoint:** `GET /client/commands/{uuid}/status`

**Response** (`200 OK`)

```json
{
  "screenshot_path": "screenshots/1/2/20260609_192530_xyz789.jpg",
  "updated_at": "2026-06-09T19:25:31.000000Z"
}
```

### 3.5 Terminate Session (Disconnect)

Clears active connection credentials (`client_uuid`, `client_token`) from the local session data store.

- **Endpoint:** `POST /client/logout`

---

## 4. Private Operator Portal (Management UI)

All endpoints listed inside this section require a stateful logged-in user profile with valid ownership records linked over the requested model resources.

### 4.1 Device Resource Manager

Standard CRUD platform operations mapped via traditional Inertia resource controllers.

| HTTP Method | Route Structure   | Component Target | Objective                                         |
| ----------- | ----------------- | ---------------- | ------------------------------------------------- |
| `GET`       | `/devices`        | `devices/index`  | List all active connected hardware hosts          |
| `GET`       | `/devices/create` | `devices/create` | Present device registration utility view          |
| `POST`      | `/devices`        | —                | Store newly provisioned hardware profile          |
| `GET`       | `/devices/{uuid}` | `devices/show`   | Core telemetry details & associated tokens        |
| `PATCH`     | `/devices/{uuid}` | —                | Modify device identity parameter                  |
| `DELETE`    | `/devices/{uuid}` | —                | Permanently isolate agent hardware configurations |

**Device Creation Request Example:**

```json
// POST /devices
{
  "name": "Backup Storage Server"
}
```

---

### 4.2 Link & Orchestration Manager

Provides generation utilities and parameters for temporary shared guest command links.

| HTTP Method | Route Structure    | Component Target  | Objective                                 |
| ----------- | ------------------ | ----------------- | ----------------------------------------- |
| `GET`       | `/commands`        | `commands/index`  | View operational log history and tokens   |
| `GET`       | `/commands/create` | `commands/create` | Generate access authorization profiles    |
| `POST`      | `/commands`        | —                 | Store link & map to target hardware layer |
| `GET`       | `/commands/{uuid}` | `commands/show`   | Management view for a specific link       |
| `PATCH`     | `/commands/{uuid}` | —                 | Update operational bounds/rules           |
| `DELETE`    | `/commands/{uuid}` | —                 | Terminate access configurations           |

**Link Update Configuration Example:**

```json
// PATCH /commands/018f3c5d-ee33-8abd-93bc-3456789012ef
{
  "name": "Temporary Support Session",
  "device_id": 2,
  "is_public": true,
  "permissions": "control",
  "expires_at": "2026-06-15 00:00:00"
}
```

#### Token Rotation Hook

Instantly invalidates old communication vectors by generating a fresh token signature for an active command instance.

- **Endpoint:** `POST /commands/{uuid}/rotate-token`
- **Response:** Redirects back with a flashed success session validation banner.

---

### 4.3 Operator Remote Control Dashboard

The main workstation viewport context for authenticated device owners to view execution streams and pipe sequential commands back to the server.

- **Endpoint:** `GET /commands/{uuid}/controller`
- **Response:** Inertia Render (`commands/controller`)

#### Private Real-time Input Pipe

Provides real-time payload updates directly to the target execution layer.

- **Endpoint:** `POST /commands/{uuid}/payload`
- **Rate Limit:** 200 req / min (High throughput optimization)
- **Payload Structure:** Matches section **3.3** exactly.
