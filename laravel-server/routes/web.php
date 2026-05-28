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
Route::get('/client', [ClientController::class, 'index'])->name('client.index');
Route::post('/client/authenticate', [ClientController::class, 'authenticate'])->name('client.authenticate');
Route::post('/client/logout', [ClientController::class, 'logout'])->name('client.logout');
Route::post('/client/commands/{command:uuid}/payload', [ClientController::class, 'storePayload'])->name('client.payload');


// ==========================================
// PRIVATE ROUTES (Requires User Login)
// ==========================================
Route::middleware(['auth', 'verified'])->group(function () {
    Route::inertia('dashboard', 'dashboard')->name('dashboard');

    Route::get('users/{user}', [UserController::class, 'show'])->name('users.show');
    Route::patch('users/{user}', [UserController::class, 'update'])->name('users.update');

    // The Private Web Controller View
    Route::get('/commands/{command:uuid}/controller', [CommandController::class, 'controller'])
        ->name('commands.controller');

    // The Private Payload Submission
    Route::post('/commands/{command:uuid}/payload', [CommandController::class, 'storePayload'])
        ->name('commands.payload');

    Route::resource('devices', DeviceController::class);

    Route::post('/commands/{command}/rotate-token', [CommandController::class, 'rotateToken'])->name('commands.rotate');
    Route::resource('commands', CommandController::class);
});

require __DIR__ . '/settings.php';
