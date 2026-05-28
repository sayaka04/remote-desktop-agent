<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class ClientRequestResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'command_id'         => $this->id,
            'uuid'               => $this->uuid,
            'name'               => $this->name,
            'screenshot_path'    => $this->screenshot_path,

            'device'             => $this->whenLoaded('device'),
            'device_uuid'        => $this->whenLoaded('device', fn() => $this->device->uuid),

            'payload' => [
                'actions' => collect($this->client_payload['actions'] ?? [])->map(function ($action) {
                    return array_filter([
                        'type'      => $action['type'] ?? 'unknown',
                        'x'         => $action['x'] ?? null,
                        'y'         => $action['y'] ?? null,
                        'button'    => $action['button'] ?? null,
                        'amount'    => $action['amount'] ?? null,
                        'axis'      => $action['axis'] ?? null,
                        'text'      => $action['text'] ?? null,
                        'key'       => $action['key'] ?? null,
                        'modifiers' => $action['modifiers'] ?? null,
                    ], fn($value) => !is_null($value));
                })->values()->all()
            ],

            'has_client_request' => $this->has_client_request,
            'has_host_response'  => $this->has_host_response,
            'requested_at'       => $this->created_at->toIso8601String(),
            'last_updated_at'    => $this->updated_at->toIso8601String(),
        ];
    }
}
