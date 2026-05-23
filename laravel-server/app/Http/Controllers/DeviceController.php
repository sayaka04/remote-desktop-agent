<?php

namespace App\Http\Controllers;

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
        $devices = Device::all();
        return inertia('devices/index', [
            'devices' => $devices
        ]);
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        return inertia('devices/create');
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        $request->validate([
            'name' => ['required', 'string', 'max:255'],
        ]);

        $device = new Device();
        $device->name = $request->input('name');
        $device->user_id = auth()->id();
        $device->uuid = Str::uuid7();
        $device->save();

        return back()->with('success', 'Device registered successfully.');
    }

    public function share(Request $request): array
    {
        return array_merge(parent::share($request), [
            'flash' => [
                'success' => fn() => $request->session()->get('success'),
                'error' => fn() => $request->session()->get('error'),
            ],
        ]);
    }

    /**
     * Display the specified resource.
     */
    public function show($device)
    {
        $device = Device::with('commands')
            ->where('uuid', $device)
            ->firstOrFail();

        return inertia('devices/show', [
            'device' => $device
        ]);
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
    public function update(Request $request, $device)
    {
        $device = Device::where('uuid', $device)->firstOrFail();
        $device->name = $request->input('name');
        $device->save();

        return redirect()->route('devices.show', ['device' => $device->uuid])->with('success', 'Device updated successfully.');
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy($device)
    {
        $device = Device::where('uuid', $device)->firstOrFail();
        $device->delete();

        return redirect()
            ->route('devices.index')
            ->with('success', 'Device deleted successfully.');
    }
}
