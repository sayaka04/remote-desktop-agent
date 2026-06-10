# Remote Command Orchestration Server (`laravel-server`)

This directory houses the **`laravel-server`** component of the Remote Command Orchestration platform. Built using **Laravel 13**, **React**, and **Inertia.js**, this application serves as the central orchestration hub, database core, and visual control plane for the entire ecosystem.

---

## 🎯 Purpose & Monorepo Context

Within the broader monorepo, this application acts as the central engine connecting three disparate entities:

1. **The Java Desktop Agent:** The background host that executes physical OS commands and captures screen buffers.
2. **The Android Remote Sender:** A native mobile client used to dispatch low-latency input arrays to the server.
3. **This Laravel Server (With Built-In Web Senders):** In addition to serving as an API relay for the external Java agent and Android app, this server **features its own robust, built-in web-based remote senders**.

Through a single unified React/Inertia frontend interface, the server acts as an independent controller, presenting two primary control environments:

- **Private Operator Dashboard:** A fully authenticated environment where device owners can create temporary control configurations, view hardware health data, rotate access signatures, and pipe real-time input macros into selected host displays.
- **Public Client Portal (`/client/*`):** An unauthenticated, stateful, session-cookie protected gateway. It allows temporary guest users possessing a unique UUIDv7 link and an unhashed 64-character token to directly pilot, view, and input actions into a remote system entirely out of their local web browser.

---

## 🚀 Key Server Features

- **Dual-Authentication Architecture:** Exposes a stateless Laravel Sanctum bearer middleware layer (`/api/*`) for external Android client and Java agent automation, while maintaining secure stateful Laravel session cookies (`web` guard) for browser users.
- **Asynchronous Sequence Pipeline:** Designed for high-frequency keyboard and pointer relays. The built-in React canvas panels transform cursor triggers into geometric coordinate maps, throwing them directly into optimized JSON database wrappers that bypass structural Inertia layout rendering overhead.
- **Dynamic Frame Storage Engine:** Processes binary multipart streams uploaded by target daemons, cleanly organizing and provisioning runtime snapshot paths under isolated local storage directories (`storage/app/public/screenshots/{user_id}/{device_id}/`).
- **Granular Throttling Filters:** Strict customized endpoint boundaries mitigate vector threats across various interface contexts, spanning from strict brute-force blocks on auth gates up to highly permissive throughput bands (200 req/min) for real-time console input relays.

---

## 🛠️ Prerequisites

Ensure your local development environment supports the following baseline dependencies:

- **PHP**: `^8.3` or higher
- **Node.js**: `^22.13`
- **Database Engine**: MySQL `^8.4.3`

---

## 📦 Setup & Installation Instructions

Since this project resides within a sub-directory of a monorepo structure, ensure your terminal workspace context is explicitly pointing to this folder before executing setup arrays.

### 1. Vendor Packages Resolution

Fetch and optimize standard application system modules, framework dependencies, and frontend compiling nodes:

```bash
composer install
npm install

```

### 2. Configure Environment Properties

Clone the distributed properties template file to establish an isolated environment execution profile:

```bash
cp .env.example .env

```

Open the freshly instantiated `.env` configuration file inside your workspace code editor and adjust the core attributes matching your targeted database engine and domain parameters:

```env
APP_NAME="Remote Orchestration Server"
APP_ENV=local
APP_KEY=
APP_DEBUG=true
APP_URL=http://localhost:8000

DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=your_database
DB_USERNAME=your_username
DB_PASSWORD=your_password

FILESYSTEM_DISK=local
SESSION_DRIVER=database
SESSION_LIFETIME=120

```

### 3. Establish System Cryptography & Hydration

Generate the secure encryption baseline hash key, process relational application structural blueprints (migrating tables for Users, Devices, and Command Logs), and map the localized display-capture asset matrix:

```bash
php artisan key:generate
php artisan migrate
php artisan storage:link

```

---

## 🖥️ Launching the Application

To drive both the backend application container layer and the hot-swapping frontend layout bundle compiler seamlessly, run the following distinct worker routines across isolated shell terminals:

- **Terminal Window 1: HTTP App Kernel Engine**

```bash
php artisan serve --port=8000

```

- **Terminal Window 2: Vite Hot-Module Compiling Routine**

```bash
npm run dev

```

Or simply run

- **Terminal Window 1: Run both at once**

```bash
composer run dev

```

Once processes report initialized running states, direct your destination browser connection context to: **`http://localhost:8000`**.
