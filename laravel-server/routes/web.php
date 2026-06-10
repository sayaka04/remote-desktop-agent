<?php

use App\Http\Controllers\ClientController;
use App\Http\Controllers\CommandController;
use App\Http\Controllers\DeviceController;
use App\Http\Controllers\UserController;
use Illuminate\Support\Facades\Route;
use Laravel\Fortify\Features;

Route::inertia('/', 'welcome', [
    'canRegister' => Features::enabled(Features::registration()),
])->name('home');

// ==========================================
// PUBLIC ROUTES (No Login Required)
// ==========================================
// Allow normal browsing (60 requests per minute)
Route::middleware('throttle:30,1')->group(function () {
    Route::get('/client', [ClientController::class, 'index'])->name('client.index');
    Route::post('/client/logout', [ClientController::class, 'logout'])->name('client.logout');
});

// STRICT Rate Limiting for Authentication (Prevent Brute-Force Password/Token guessing)
// Only allow 5 attempts per minute per IP.
Route::middleware('throttle:100,1')->group(function () {
    Route::post('/client/authenticate', [ClientController::class, 'authenticate'])->name('client.authenticate');
});

// HIGHER Rate Limiting for Payload Submission (Since Axios sends requests instantly)
// Allows 120 actions per minute
Route::middleware('throttle:120,1')->group(function () {
    Route::post('/client/commands/{command:uuid}/payload', [ClientController::class, 'storePayload'])->name('client.payload');

    Route::get('/client/commands/{command:uuid}/status', [ClientController::class, 'status'])
        ->name('client.status');
});

// ==========================================
// PRIVATE ROUTES (Requires User Login)
// ==========================================
Route::middleware(['auth', 'verified', 'throttle:100,1'])->group(function () {
    Route::inertia('dashboard', 'dashboard')->name('dashboard');

    Route::get('/commands/{command:uuid}/status', [CommandController::class, 'status'])
        ->name('commands.status');


    // Route::get('users/{user}', [UserController::class, 'show'])->name('users.show');
    // Route::patch('users/{user}', [UserController::class, 'update'])->name('users.update');

    Route::get('/commands/{command:uuid}/controller', [CommandController::class, 'controller'])
        ->name('commands.controller');

    // High limit for private payload execution
    Route::post('/commands/{command:uuid}/payload', [CommandController::class, 'storePayload'])
        ->middleware('throttle:200,1')
        ->name('commands.payload');

    Route::resource('devices', DeviceController::class);

    Route::post('/commands/{command}/rotate-token', [CommandController::class, 'rotateToken'])->name('commands.rotate');
    Route::resource('commands', CommandController::class);
});

require __DIR__ . '/settings.php';
