<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     *
     * Drops the static name snapshot columns (performed_by, target_user_name)
     * and adds a target_type column to enable dynamic name resolution via
     * Eloquent relationships in ActivityLogResource.
     */
    public function up(): void
    {
        Schema::table('activity_logs', function (Blueprint $table) {
            $table->dropColumn(['performed_by', 'target_user_name']);
            $table->string('target_type')->default('user')->after('target_user_id');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('activity_logs', function (Blueprint $table) {
            $table->dropColumn('target_type');
            $table->string('performed_by')->after('actor_id');
            $table->string('target_user_name')->after('target_user_id');
        });
    }
};
