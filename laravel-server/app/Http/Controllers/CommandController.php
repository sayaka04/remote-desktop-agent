<?php

namespace App\Http\Controllers;

use App\Models\Command;
use App\Models\Device;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Str;
use Inertia\Inertia;

class CommandController extends Controller
{
    public function index()
    {
        $commands = Command::with("device")->whereHas("device", function ($query) {
            $query->where('user_id', Auth::id());
        })->get();

        return inertia('commands/index', [
            'commands' => $commands
        ]);
    }

    public function create()
    {
        $devices = Device::where('user_id', Auth::id())->get();
        return inertia('commands/create', [
            'devices' => $devices
        ]);
    }

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
     * Display the private Web Controller for the device owner.
     */
    public function controller(Command $command)
    {
        // Security check
        if ($command->device->user_id !== Auth::id()) {
            abort(403);
        }

        $command->load('device');

        // FIX: Match your existing lowercase folder and file structure perfectly
        return inertia('commands/controller', [
            'command' => $command
        ]);
    }

    /**
     * Store the action payload securely from the private web controller.
     */
    public function storePayload(Request $request, Command $command)
    {
        if ($command->device->user_id !== Auth::id()) {
            abort(403);
        }

        $validated = $request->validate([
            'payload' => 'required|array',
            'payload.*.type' => 'required|in:move_mouse,click,type_text',
            'payload.*.x' => 'nullable|numeric',
            'payload.*.y' => 'nullable|numeric',
            'payload.*.button' => 'nullable|in:left,middle,right',
            'payload.*.text' => 'nullable|string',
        ]);

        $command->update([
            'client_payload'     => ['actions' => $validated['payload']],
            'has_client_request' => true,
            'has_host_response'  => false,
        ]);

        return back();
    }

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

    public function destroy(Command $command)
    {
        $command->delete();
        return redirect()->route('commands.index')->with('success', 'Command deleted successfully.');
    }
}
