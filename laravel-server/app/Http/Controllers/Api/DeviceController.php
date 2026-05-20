<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Device;
use Illuminate\Http\Request;
use Illuminate\Support\Str;

class DeviceController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $devices = Device::where('user_id', auth()->id())->get();
        return response()->json($devices);
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        //
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        // Validate the incoming request
        $validated = $request->validate([
            'name'    => 'required|string|max:255',
            // For testing only
            //TODO: Remove this user_id validation and instead use the authenticated user from the token
            'user_id' => 'required|exists:users,id',
        ]);

        // Create the device and automatically generate a secure UUID
        $device = Device::create([
            'user_id' => $validated['user_id'],
            'name'    => $validated['name'],
            'uuid'    => Str::uuid(), // Generates a unique string like "550e8400-e29b-41d4-a716-446655440000"
        ]);

        return response()->json([
            'message' => 'Device created successfully',
            'device'  => $device
        ], 201);
    }

    /**
     * Display the specified resource.
     */
    public function show(Device $device)
    {
        //
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Device $device)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, Device $device)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Device $device)
    {
        //
    }


    /**
     * Get the list of available commands for a specific device.
     */
    public function commands(Device $device)
    {
        // This ensures a user can only see commands for their own device
        if ($device->user_id !== auth()->id()) {
            return response()->json(['message' => 'Unauthorized'], 403);
        }

        // Return the commands belonging to this device
        // This matches the DeviceCommand model in your Android code (id, name, description)
        return response()->json($device->commands);
    }
}
