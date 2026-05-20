<?php

namespace App\Http\Controllers\Api;

use App\Enums\ActionType;
use App\Http\Controllers\Controller;
use App\Http\Resources\ClientRequestResource;
use App\Http\Resources\HostResponseResource;
use App\Models\Command;
use App\Models\Device;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Validation\Rule;

class CommandController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $devices = Device::where("user_id", Auth::id())->get();
        $commands = Command::whereIn('device_id', $devices->pluck('id'))->get();
        return response()->json($commands);
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        //
    }

    /**
     * 1. INITIALIZE COMMAND: Creates a blank command record and returns the ID.
     */
    public function store(Request $request)
    {
        $validated = $request->validate([
            'device_id' => 'required|exists:devices,id',
        ]);

        // Create the empty command wrapper
        $command = Command::create([
            'device_id'          => $validated['device_id'],
            'client_payload'     => [],    // Empty for now
            'has_client_request' => false, // False so the host doesn't try to fetch it yet
            'has_host_response'  => false,
        ]);

        return response()->json([
            'message'    => 'Command created successfully',
            'command_id' => $command->id
        ], 201);
    }

    /**
     * 2. CLIENT REQUEST (UPDATE): Android app attaches the payload to the specific command.
     */
    public function updateClientRequest(Request $request, Command $command)
    {
        // 1. Validate incoming data from the Android Client
        $validated = $request->validate([
            'client_payload' => 'required|array',
            'client_payload.actions' => 'required|array',

            // Validates that the "type" exactly matches one of your Enum cases
            'client_payload.actions.*.type' => ['required', 'string', Rule::enum(ActionType::class)],
            // Conditional validation based on action type
            'client_payload.actions.*.x' => 'nullable|decimal:0,10',
            'client_payload.actions.*.y' => 'nullable|decimal:0,10',
            'client_payload.actions.*.button' => 'string|in:left,right,middle|nullable',
            'client_payload.actions.*.amount' => 'integer|nullable',
            'client_payload.actions.*.text' => 'string|nullable',
        ]);

        // 2. Update the existing command with the payload and FLIP the flag to true
        $command->update([
            'client_payload'     => $validated['client_payload'],
            'has_client_request' => true, // TRUE! Now the Java Host will see it and execute it.
        ]);

        // 3. Return the formatted resource
        return new ClientRequestResource($command);
    }

    /**
     * 3. HOST RESPONSE (UPDATE): Java desktop uploads results/screenshots.
     */
    public function updateHostResponse(Request $request, Command $command)
    {
        // 1. Validate incoming data from the Java Host
        $request->validate([
            'host_payload'           => 'required|array',
            'host_payload.success'   => 'required|boolean',
            'host_payload.message'   => 'required|string',
            'screenshot'             => 'nullable|image|mimes:jpeg,png,jpg|max:5120',
        ]);

        $screenshotPath = null;
        if ($request->hasFile('screenshot')) {
            $screenshotPath = $request->file('screenshot')->store('screenshots', 'public');
        }

        // 2. Update the command with results and flip the flag
        $command->update([
            'has_client_request' => false, // Keep existing client request flag
            'client_payload'     => [], // Keep existing client payload
            'host_payload'       => $request->input('host_payload'),
            'screenshot_path'    => $screenshotPath,
            'has_host_response'  => true, // TRUE! Now the Android app can read the result.
        ]);

        return new HostResponseResource($command);
    }

    /**
     * Display the specified resource.
     */
    public function show(Command $command)
    {
        return new ClientRequestResource($command);
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(Command $command)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, Command $command)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Command $command)
    {
        //
    }
}
