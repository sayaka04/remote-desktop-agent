<?php

use App\Http\Controllers\CommandController;
use App\Http\Controllers\DeviceController;
use App\Http\Controllers\UserController;
use Illuminate\Support\Facades\Route;
use Laravel\Fortify\Features;

Route::inertia('/', 'welcome', [
    'canRegister' => Features::enabled(Features::registration()),
])->name('home');

Route::middleware(['auth', 'verified'])->group(function () {
    Route::inertia('dashboard', 'dashboard')->name('dashboard');



    // --- New additions
    Route::get('users/{user}', [UserController::class, 'show'])->name('users.show');

    Route::patch('users/{user}', [UserController::class, 'update'])->name('users.update');

    Route::resource('devices', DeviceController::class);

    Route::post('/commands/{command}/rotate', [CommandController::class, 'rotateToken'])->name('commands.rotate');
    Route::resource('commands', CommandController::class);
});

require __DIR__ . '/settings.php';
