<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\CommandController;
use App\Http\Controllers\Api\DeviceController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

Route::post('/login', [AuthController::class, 'login']);



Route::middleware('auth:sanctum')->group(function () {

    Route::get('/user', function (Request $request) {
        return $request->user();
    });

    Route::get('/devices', [DeviceController::class, 'index']);
    Route::get('devices/{device}/commands', [DeviceController::class, 'commands']);

    Route::get('/commands', [CommandController::class, 'index']);


    Route::post('/logout', [AuthController::class, 'logout']);

    Route::post('/devices', [DeviceController::class, 'store']);

    Route::post('/commands', [CommandController::class, 'store']);

    Route::get('/commands/{command}', [CommandController::class, 'show']);

    Route::post('/commands/{command}/request', [CommandController::class, 'updateClientRequest']);

    Route::post('/commands/{command}/response', [CommandController::class, 'updateHostResponse']);
});

Route::get('/user', function (Request $request) {
    return $request->user();
})->middleware('auth:sanctum');

Route::get('/test', function () {
    return response()->json(['message' => 'API is working!']);
});
