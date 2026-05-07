<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class ClientRequestResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {

        return [
            'command_id'         => $this->id,

            'device_uuid'        => $this->whenLoaded('device', function () {
                return $this->device->uuid;
            }),

            'payload' => [
                'actions' => (isset($this->client_payload['actions']) && is_array($this->client_payload['actions']))
                    ? collect($this->client_payload['actions'])->map(function ($action) {
                        return array_filter([
                            'type'   => $action['type'] ?? 'unknown',
                            'x'      => $action['x'] ?? null,
                            'y'      => $action['y'] ?? null,
                            'button' => $action['button'] ?? null,
                            'amount' => $action['amount'] ?? null,
                            'text'   => $action['text'] ?? null,
                        ], fn($value) => !is_null($value));
                    })->values()->all()
                    : [] // If it's just [], return an empty actions list
            ],

            // The flags
            'has_client_request'    => $this->has_client_request,
            'has_host_response'   => $this->has_host_response,

            // Timestamps are useful for the Android app to calculate timeouts
            'requested_at'       => $this->created_at->toIso8601String(),
            'last_updated_at'    => $this->updated_at->toIso8601String(),
        ];
    }
}
