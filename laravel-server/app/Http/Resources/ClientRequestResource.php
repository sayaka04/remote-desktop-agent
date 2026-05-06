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
                'actions' => collect($this->client_payload['actions'])->map(function ($action) {
                    // Create array and immediately filter out NULL values
                    return array_filter([
                        'type'   => $action['type'],
                        'x'      => $action['x'] ?? null,
                        'y'      => $action['y'] ?? null,
                        'button' => $action['button'] ?? null,
                        'amount' => $action['amount'] ?? null,
                        'text'   => $action['text'] ?? null,
                    ], function ($value) {
                        return !is_null($value); // Only keep the key if the value is not null
                    });
                })->values()->all()
            ],

            // The flags
            'is_pending_host'    => $this->has_client_request,
            'is_host_finished'   => $this->has_host_response,

            // Timestamps are useful for the Android app to calculate timeouts
            'requested_at'       => $this->created_at->toIso8601String(),
            'last_updated_at'    => $this->updated_at->toIso8601String(),
        ];
    }
}
