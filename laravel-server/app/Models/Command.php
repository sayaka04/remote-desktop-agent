<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Command extends Model
{
    use HasFactory;

    /**
     * The attributes that are mass assignable.
     */
    protected $fillable = [
        'device_id',
        'uuid',
        'name',
        'is_public',
        'permissions',
        'expires_at',
        'access_token',
        'has_client_request',
        'client_payload',
        'has_host_response',
        'host_payload',
        'screenshot_path',
    ];

    /**
     * The attributes that should be cast.
     */
    protected $casts = [
        'is_public'          => 'boolean', 
        'permissions'        => 'string', 
        'expires_at'         => 'datetime',
        'has_client_request' => 'boolean',
        'has_host_response'  => 'boolean',
        'client_payload'     => 'array',
        'host_payload'       => 'array',
    ];

    /**
     * Use the UUID for routing instead of the ID.
     * This makes URLs look like: /commands/018f3a...
     */
    public function getRouteKeyName(): string
    {
        return 'uuid';
    }

    /**
     * Get the device that this command belongs to.
     */
    public function device(): BelongsTo
    {
        return $this->belongsTo(Device::class);
    }

    /**
     * Helper to check if the session is still valid.
     */
    public function isValid(): bool
    {
        if (!$this->is_public) return false;
        if ($this->expires_at && $this->expires_at->isPast()) return false;

        return true;
    }
}
