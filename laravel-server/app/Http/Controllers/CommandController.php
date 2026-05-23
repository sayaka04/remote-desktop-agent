<?php

namespace App\Http\Controllers;

use App\Models\Command;
use App\Models\Device;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Str;

class CommandController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $commands = Command::with("device")->whereHas("device", function ($query) {
            $query->where('user_id', Auth::id());
        })->get();

        return inertia('commands/index', [
            'commands' => $commands
        ]);
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        $devices = Device::where('user_id', Auth::id())->get();
        return inertia('commands/create', [
            'devices' => $devices
        ]);
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        $validated = $request->validate([
            'device_id' => ['required', 'exists:devices,id'],
            'name' => ['required', 'string', 'max:255']
        ]);

        Command::create([
            'device_id'          => $validated['device_id'],
            'uuid'               => (string) Str::uuid7(),
            'access_token'       => Str::random(64),
            'name'               => $validated['name'],
            'client_payload'     => [],
            'has_client_request' => false,
            'has_host_response'  => false,
        ]);

        return back()->with('success', 'Command created successfully.');
    }

    /**
     * Rotate the public access token.
     */
    public function rotateToken(Command $command)
    {
        if ($command->device->user_id !== Auth::id()) {
            abort(403);
        }

        $command->update([
            'access_token' => Str::random(64)
        ]);

        return back()->with('success', 'The access token has been regenerated.');
    }

    /**
     * Display the specified resource.
     */
    public function show(Command $command)
    {
        if ($command->device->user_id !== Auth::id()) {
            abort(403);
        }

        $command->load('device');

        return inertia('commands/show', [
            'command' => $command,
            'devices' => Device::where('user_id', Auth::id())->get(['id', 'name'])
        ]);
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, Command $command)
    {
        if ($command->device->user_id !== Auth::id()) {
            abort(403);
        }

        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'device_id' => 'required|exists:devices,id',
            'is_public' => 'boolean',
            'permissions' => 'string',
            'expires_at' => 'nullable|date',
        ]);

        $command->update($validated);

        return redirect()->back()->with('success', 'Command updated successfully.');
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Command $command)
    {
        $command->delete();
        return redirect()->route('commands.index')->with('success', 'Command deleted successfully.');
    }
}
