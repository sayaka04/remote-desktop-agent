<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\CommandController;
use App\Http\Controllers\Api\DeviceController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

// ==========================================
// PUBLIC API ROUTES
// ==========================================

// STRICT THROTTLE: Prevent Brute-Force Attacks on Login.
// Only allows 5 attempts per minute per IP address.
Route::middleware('throttle:5,1')->post('/login', [AuthController::class, 'login']);

// Standard limit for the test route (60 requests per minute)
Route::middleware('throttle:60,1')->get('/test', function () {
    return response()->json(['message' => 'API is working!']);
});


// ==========================================
// PROTECTED API ROUTES (Requires Sanctum Token)
// ==========================================
Route::middleware(['auth:sanctum'])->group(function () {

    // STANDARD THROTTLE: 60 requests per minute for general data fetching
    Route::middleware('throttle:60,1')->group(function () {
        Route::get('/user', function (Request $request) {
            return $request->user();
        });

        Route::post('/logout', [AuthController::class, 'logout']);

        // Devices
        Route::get('/devices', [DeviceController::class, 'index']);
        Route::post('/devices', [DeviceController::class, 'store']);
        Route::get('devices/{device}/commands', [DeviceController::class, 'commands']);

        // Commands (Creation and Viewing)
        Route::get('/commands', [CommandController::class, 'index']);
        Route::post('/commands', [CommandController::class, 'store']);
        Route::get('/commands/{command}', [CommandController::class, 'show']);
    });

    // HIGH THROTTLE: 200 requests per minute (approx 3 per second)
    // We set this higher because your Android/Java hosts might need to poll 
    // frequently or rapidly upload screenshots/command results in sequence.
    Route::middleware('throttle:200,1')->group(function () {
        Route::post('/commands/{command}/request', [CommandController::class, 'updateClientRequest']);
        Route::post('/commands/{command}/response', [CommandController::class, 'updateHostResponse']);
    });
});
