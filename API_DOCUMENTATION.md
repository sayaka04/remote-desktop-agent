# Remote Command API Documentation

This API facilitates communication between an **Android Client** (sender) and a **Java Desktop Host** (executor).

## Endpoints Summary

| Method | Endpoint                      | Description                           |
| :----- | :---------------------------- | :------------------------------------ |
| `POST` | `/api/login`                  | Login to receive Bearer Token         |
| `POST` | `/api/devices`                | Register a new desktop host           |
| `POST` | `/api/commands`               | Initialize a new command record       |
| `POST` | `/api/commands/{id}/request`  | Android: Send payload/instructions    |
| `POST` | `/api/commands/{id}/response` | Java Host: Submit result & screenshot |

---

## 1. Authentication (Login)

**URL:** `/api/login`  
**Description:** Exchange credentials for a Bearer Token.

### Request Body (JSON)

```json
{
  "email": "admin@email.com",
  "password": "password"
}
```

### Response (200 OK)

```json
{
  "token": "1|ra68KDSYvJ2X..."
}
```

---

## 2. Register Device

**URL:** `/api/devices`  
**Description:** Used to register the desktop agent and get a `device_id`.

### Request Body (JSON)

```json
{
  "name": "My Windows Desktop",
  "user_id": 1
}
```

---

## 3. Initialize Command

**URL:** `/api/commands`  
**Description:** The Android app calls this first to get a unique `command_id`.

### Request Body (JSON)

```json
{
  "device_id": 1
}
```

### Response (201 Created)

```json
{
  "message": "Command created successfully",
  "command_id": 45
}
```

---

## 4. Submit Client Request (Android)

**URL:** `/api/commands/{id}/request`  
**Description:** Attaches the specific actions to the command record.

### Request Body (JSON)

```json
{
  "client_payload": {
    "actions": [
      { "type": "move_mouse", "x": 500, "y": 300 },
      { "type": "click", "button": "left" },
      { "type": "type_text", "text": "Hello World" }
    ]
  }
}
```

---

## 5. Submit Host Response (Java Host)

**URL:** `/api/commands/{id}/response`  
**Description:** The Java desktop agent uploads execution results and a screenshot.

### Request Body (Multipart Form-Data)

| Key                     | Type     | Value           | Description                 |
| :---------------------- | :------- | :-------------- | :-------------------------- |
| `host_payload[success]` | Text     | `1`             | `1` for true, `0` for false |
| `host_payload[message]` | Text     | `Task Finished` | Success or error message    |
| `screenshot`            | **File** | `image.png`     | JPG/PNG up to 5MB           |

---

## Postman Testing Guide

1.  **Headers:** Always include `Accept: application/json` on every request.
2.  **Auth:** Go to the **Auth** tab in Postman, select **Bearer Token**, and paste the token from Step 1.
3.  **Storage:** Run `php artisan storage:link` to make `screenshot_url` accessible.
