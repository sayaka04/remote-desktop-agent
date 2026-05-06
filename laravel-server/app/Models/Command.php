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
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'device_id',
        'has_client_request',
        'client_payload',
        'has_host_response',
        'host_payload',
        'screenshot_path',
    ];

    /**
     * The attributes that should be cast.
     *
     * @var array<string, string>
     */
    protected $casts = [
        'has_client_request' => 'boolean',
        'has_host_response'  => 'boolean',
        'client_payload'     => 'array', // Automatically casts JSON to PHP array
        'host_payload'       => 'array', // Automatically casts JSON to PHP array
    ];

    /**
     * Get the device that this command belongs to.
     */
    public function device(): BelongsTo
    {
        return $this->belongsTo(Device::class);
    }
}
