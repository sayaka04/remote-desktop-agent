<?php

namespace App\Http\Controllers;

use App\Models\Command;
use Illuminate\Http\Request;
use Inertia\Inertia;

class ClientController extends Controller
{
    public function index(Request $request)
    {
        // 1. Read securely from the encrypted session cookie
        $uuid = $request->session()->get('client_uuid');
        $token = $request->session()->get('client_token');
        $activeCommand = null;

        if ($uuid && $token) {
            $activeCommand = Command::with('device')
                ->where('uuid', $uuid)
                ->where('access_token', $token)
                ->where('is_public', true)
                ->first();

            // If the session data is invalid or expired, clear it out securely
            if (!$activeCommand) {
                $request->session()->forget(['client_uuid', 'client_token']);
            }
        }

        return Inertia::render('client/index', [
            'activeCommand' => $activeCommand,
        ]);
    }

    /**
     * Authenticate via POST request (Hidden from URL)
     */
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
            // Save securely to the server-side session
            $request->session()->put('client_uuid', $request->uuid);
            $request->session()->put('client_token', $request->token);
            return back();
        }

        return back()->withErrors(['auth' => 'Invalid or expired credentials.']);
    }

    /**
     * Clear the session via POST
     */
    public function logout(Request $request)
    {
        $request->session()->forget(['client_uuid', 'client_token']);
        return back();
    }

    /**
     * Store the payload (Notice we no longer need the token from the frontend!)
     */
    public function storePayload(Request $request, Command $command)
    {
        // Verify identity using the secure session token instead of the request body
        if ($command->access_token !== $request->session()->get('client_token')) {
            abort(403, 'Unauthorized access.');
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
}
