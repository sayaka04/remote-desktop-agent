# Remote Command Orchestration API Reference

## Overview

The REST API provides headless, secure integration for external clients (such as Android devices) and execution hosts (such as Java Desktop agents). It facilitates authentication, device registration, and a bidirectional command orchestration system.

## Base URL

All API requests should be prefixed with `/api` relative to your application's base domain.

```text
https://yourdomain.com/api

```

## Authentication

This API uses **Laravel Sanctum** for token-based authentication.
With the exception of `/login` and `/test`, all endpoints require a valid Bearer token passed in the `Authorization` header of your HTTP request.

```http
Authorization: Bearer <your_api_token>
Accept: application/json

```

## Rate Limiting

The API enforces rate limiting per IP address to ensure stability and security. Headers containing current rate limit status (`X-RateLimit-Limit`, `X-RateLimit-Remaining`) are returned with every request.

| Context           | Limit         | Endpoints                                                         |
| ----------------- | ------------- | ----------------------------------------------------------------- |
| **Strict (Auth)** | 5 req / min   | `POST /login`                                                     |
| **Standard**      | 60 req / min  | `GET /*`, `POST /logout`, `POST /devices`, `POST /commands`       |
| **High Volume**   | 200 req / min | `POST /commands/{uuid}/request`, `POST /commands/{uuid}/response` |

---

## 1. Authentication

### 1.1 Authenticate User (Login)

Exchanges user credentials for an API Bearer token.

- **Endpoint:** `POST /login`
- **Access:** Public

**Request Body** (`application/json`)

```json
{
  "email": "operator@example.com",
  "password": "securepassword123"
}
```

**Response** (`200 OK`)

```json
{
  "user": {
    "id": 1,
    "name": "Admin Operator",
    "email": "operator@example.com",
    "created_at": "2026-05-06T04:45:22.000000Z"
  },
  "token": "1|abc123def456ghi789..."
}
```

### 1.2 Revoke Token (Logout)

Invalidates the currently authenticated token.

- **Endpoint:** `POST /logout`
- **Access:** Authenticated

**Response** (`200 OK`)

```json
{
  "message": "Logged out successfully"
}
```

### 1.3 Get Authenticated User

Retrieves the profile of the currently authenticated user.

- **Endpoint:** `GET /user`
- **Access:** Authenticated

---

## 2. Devices

### 2.1 List Devices

Retrieves a list of all devices owned by the authenticated user.

- **Endpoint:** `GET /devices`
- **Access:** Authenticated

**Response** (`200 OK`)

```json
[
  {
    "id": 1,
    "uuid": "018f3a2b-cd14-7abc-91ef-2234567890ab",
    "user_id": 1,
    "name": "Main Office Workstation",
    "last_seen_at": "2026-06-09T18:00:00.000000Z",
    "created_at": "2026-06-08T10:00:00.000000Z"
  }
]
```

### 2.2 Register Device

Registers a new execution host and automatically generates a secure UUID7.

- **Endpoint:** `POST /devices`
- **Access:** Authenticated

**Request Body** (`application/json`)

```json
{
  "name": "Living Room PC",
  "user_id": 1
}
```

_(Note: `user_id` is currently required in the payload for testing architecture purposes)._

**Response** (`201 Created`)

```json
{
  "message": "Device created successfully",
  "device": {
    "id": 2,
    "uuid": "018f3b4c-da22-7fca-82ab-1234567890cd",
    "user_id": 1,
    "name": "Living Room PC",
    "updated_at": "2026-06-09T19:22:00.000000Z",
    "created_at": "2026-06-09T19:22:00.000000Z"
  }
}
```

### 2.3 List Device Commands

Retrieves the command history for a specific device.

- **Endpoint:** `GET /devices/{uuid}/commands`
- **Access:** Authenticated (Must own the device)

---

## 3. Command Orchestration

_Note: In the URLs below, `{uuid}` refers to the `uuid` field of the command, as route model binding uses UUIDs rather than standard IDs._

### 3.1 Initialize Command Wrapper

Creates a blank command record. Used prior to sending heavy payload requests.

- **Endpoint:** `POST /commands`
- **Access:** Authenticated

**Request Body** (`application/json`)

```json
{
  "device_id": 2
}
```

**Response** (`201 Created`)

```json
{
  "id": 45,
  "device_id": 2,
  "uuid": "018f3c5d-ee33-8abd-93bc-3456789012ef",
  "has_client_request": false,
  "has_host_response": false,
  "client_payload": [],
  "host_payload": null,
  "screenshot_path": null
}
```

### 3.2 List All Commands

Retrieves all command sessions across all devices owned by the user.

- **Endpoint:** `GET /commands`
- **Access:** Authenticated

### 3.3 Fetch Specific Command

Retrieves the exact state of a targeted command. Used by clients polling for host responses.

- **Endpoint:** `GET /commands/{uuid}`
- **Access:** Authenticated

### 3.4 Submit Client Request (Android Senders)

Attaches operational sequence payloads to an active command. This alters the internal state to flag that a request is ready for the Java Host to process.

- **Endpoint:** `POST /commands/{uuid}/request`
- **Access:** Authenticated

**Request Body** (`application/json`)

```json
{
  "client_payload": {
    "actions": [
      { "type": "move_mouse", "x": 1024, "y": 768 },
      { "type": "click", "button": "left" },
      { "type": "type_text", "text": "Deploying update..." }
    ]
  }
}
```

### 3.5 Submit Host Response (Java Executors)

The destination endpoint for Java desktop agents to upload execution results and display snapshots back to the server.

- **Endpoint:** `POST /commands/{uuid}/response`
- **Access:** Authenticated
- **Content-Type:** `multipart/form-data`

**Form Data Parameters:**

| Key            | Type            | Description                                                                |
| -------------- | --------------- | -------------------------------------------------------------------------- |
| `host_payload` | `string` (JSON) | Execution data (e.g., `{"success": true, "message": "Actions completed"}`) |
| `screenshot`   | `file` (Binary) | Current active desktop snapshot (Image file)                               |

**Response** (`200 OK`)
Returns the updated Command Resource model verifying that `has_host_response` has been successfully set to `true`.
