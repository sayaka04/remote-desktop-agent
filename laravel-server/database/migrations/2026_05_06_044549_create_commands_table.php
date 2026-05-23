<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('commands', function (Blueprint $table) {
            $table->id();
            $table->foreignId('device_id')->constrained()->cascadeOnDelete();

            // Public Identifier
            $table->uuid('uuid')->unique();
            $table->string('access_token', 64)->unique();
            $table->string('name');

            // Sharing & Security Settings
            $table->boolean('is_public')->default(false);
            $table->enum('permissions', ['view', 'control'])->default('view');
            $table->timestamp('expires_at')->nullable();

            // Request/Payload Data
            $table->boolean('has_client_request')->default(true);
            $table->json('client_payload');

            // Response/Host Data
            $table->boolean('has_host_response')->default(false);
            $table->json('host_payload')->nullable();
            $table->string('screenshot_path')->nullable();

            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('commands');
    }
};
