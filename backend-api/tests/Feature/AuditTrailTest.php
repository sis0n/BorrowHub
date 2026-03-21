<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Student;
use App\Models\Item;
use App\Models\ActivityLog;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class AuditTrailTest extends TestCase
{
    use RefreshDatabase;

    protected $admin;

    protected function setUp(): void
    {
        parent::setUp();
        $this->admin = User::factory()->create(['role' => 'admin', 'name' => 'Admin User']);
    }

    public function test_admin_can_view_activity_logs()
    {
        ActivityLog::create([
            'actor_id' => $this->admin->id,
            'performed_by' => $this->admin->name,
            'target_user_id' => '1',
            'target_user_name' => 'Test Target',
            'action' => 'Test Action',
            'details' => 'Test Details',
            'type' => 'activity'
        ]);

        $response = $this->actingAs($this->admin)->getJson('/api/v1/activity-logs');

        $response->assertStatus(200)
            ->assertJsonStructure([
                'status',
                'message',
                'data' => [
                    'data' => [
                        '*' => [
                            'id', 'actor_id', 'performed_by', 'action', 'details', 'created_at'
                        ]
                    ]
                ]
            ]);
    }

    public function test_admin_can_view_transaction_logs()
    {
        ActivityLog::create([
            'actor_id' => $this->admin->id,
            'performed_by' => $this->admin->name,
            'target_user_id' => '1',
            'target_user_name' => 'Test Student',
            'action' => 'Items Borrowed',
            'details' => 'Items: Laptop',
            'type' => 'transaction'
        ]);

        $response = $this->actingAs($this->admin)->getJson('/api/v1/transaction-logs');

        $response->assertStatus(200)
             ->assertJsonFragment(['action' => 'Items Borrowed']);
    }

    public function test_user_creation_logs_activity()
    {
        $response = $this->actingAs($this->admin)->postJson('/api/v1/users', [
            'name' => 'New User',
            'username' => 'newuser',
            'password' => 'password',
            'role' => 'staff'
        ]);

        $response->assertStatus(201);

        $this->assertDatabaseHas('activity_logs', [
            'action' => 'User Created',
            'performed_by' => $this->admin->name,
            'target_user_name' => 'New User'
        ]);
    }

    public function test_item_creation_logs_activity()
    {
        $category = \App\Models\Category::factory()->create();

        $response = $this->actingAs($this->admin)->postJson('/api/v1/items', [
            'code' => 'ITEM001',
            'name' => 'Test Item',
            'category_id' => $category->id,
            'total_quantity' => 10,
            'description' => 'Test Desc',
            'status' => 'active',
        ]);

        $response->assertStatus(201);

        $this->assertDatabaseHas('activity_logs', [
            'action' => 'Item Created',
            'target_user_name' => 'Test Item'
        ]);
    }
}
