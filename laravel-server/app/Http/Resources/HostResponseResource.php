<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class HostResponseResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'command_id'        => $this->id,
            'has_host_response' => $this->has_host_response,
            'success'           => $this->host_payload['success'] ?? false,
            'message'           => $this->host_payload['message'] ?? 'Pending',
            'screenshot_url'    => $this->screenshot_path ? asset('storage/' . $this->screenshot_path) : null,
            'completed_at'      => $this->updated_at->toIso8601String(),
        ];
    }
}
