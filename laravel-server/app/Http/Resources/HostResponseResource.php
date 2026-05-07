<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class HostResponseResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'command_id'       => $this->id,
            'has_host_response' => $this->has_host_response,

            // Extracting the conditional payload data safely
            'success'          => $this->host_payload['success'] ?? false,
            'message'          => $this->host_payload['message'] ?? 'Pending',

            // Convert the internal storage path to a full public URL for the Android App
            'screenshot_url'   => $this->screenshot_path
                ? asset('storage/' . $this->screenshot_path)
                : null,

            // Useful for showing the user exactly when it finished
            'completed_at'     => $this->updated_at->toIso8601String(),
        ];
    }
}
