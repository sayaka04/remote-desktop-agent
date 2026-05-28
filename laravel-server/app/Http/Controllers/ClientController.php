<?php

namespace App\Http\Controllers;

use App\Models\Command;
use Illuminate\Http\Request;
use Inertia\Inertia;

class ClientController extends Controller
{
    public function index(Request $request)
    {
        $uuid = $request->session()->get('client_uuid');
        $token = $request->session()->get('client_token');
        $activeCommand = null;

        if ($uuid && $token) {
            $activeCommand = Command::with('device')
                ->where('uuid', $uuid)
                ->where('access_token', $token)
                ->where('is_public', true)
                ->first();

            if (!$activeCommand) {
                $request->session()->forget(['client_uuid', 'client_token']);
            }
        }

        return Inertia::render('client/index', [
            'activeCommand' => $activeCommand,
        ]);
    }

    public function status(Command $command)
    {
        // Security: Ensure the public client is authenticated for THIS specific command
        if (session('client_command_uuid') !== $command->uuid) {
            abort(403, 'Unauthorized session');
        }

        return response()->json([
            'screenshot_path' => $command->screenshot_path,
            'updated_at' => $command->updated_at,
        ]);
    }

    public function authenticate(Request $request)
    {
        $request->validate([
            'uuid' => 'required|string',
            'token' => 'required|string',
        ]);

        $command = Command::where('uuid', $request->uuid)
            ->where('access_token', $request->token)
            ->where('is_public', true)
            ->first();

        if ($command) {
            $request->session()->put('client_uuid', $command->uuid);
            $request->session()->put('client_token', $command->access_token);
            return back()->with('success', 'Authenticated successfully.');
        }

        return back()->withErrors(['auth' => 'Invalid session credentials.']);
    }

    public function logout(Request $request)
    {
        $request->session()->forget(['client_uuid', 'client_token']);
        return back();
    }

    /**
     * Store the payload (Optimized for speed)
     */
    public function storePayload(Request $request, Command $command)
    {
        if ($command->access_token !== $request->session()->get('client_token')) {
            abort(403, 'Unauthorized access.');
        }

        $validated = $request->validate([
            'payload' => 'required|array',
            'payload.*.type' => 'required|string',
            'payload.*.x' => 'nullable|numeric',
            'payload.*.y' => 'nullable|numeric',
            'payload.*.button' => 'nullable|string',
            'payload.*.text' => 'nullable|string',
            'payload.*.key' => 'nullable|string',
            'payload.*.modifiers' => 'nullable|array',
            'payload.*.axis' => 'nullable|string',
            'payload.*.amount' => 'nullable|numeric',
        ]);

        $command->update([
            'client_payload'     => ['actions' => $validated['payload']],
            'has_client_request' => true,
            'has_host_response'  => false,
        ]);

        // SPEED IMPROVEMENT: Return JSON instead of back()
        // This stops the browser from reloading the entire Inertia state.
        return response()->json([
            'success' => true,
            'message' => 'Command sequence transmitted.'
        ]);
    }
}
