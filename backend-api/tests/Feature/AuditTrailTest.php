<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Student;
use App\Models\Item;
use App\Models\ActivityLog;
use App\Services\LogService;
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
            'action' => LogService::ACTION_CREATED,
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
            'action' => LogService::ACTION_BORROWED,
            'details' => 'Borrowed items: Laptop (1)',
            'type' => 'transaction'
        ]);

        $response = $this->actingAs($this->admin)->getJson('/api/v1/transaction-logs');

        $response->assertStatus(200)
             ->assertJsonFragment(['action' => LogService::ACTION_BORROWED]);
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
            'action' => LogService::ACTION_CREATED,
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
            'action' => LogService::ACTION_CREATED,
            'target_user_name' => 'Test Item'
        ]);
    }

    public function test_activity_logs_can_be_filtered_by_action()
    {
        ActivityLog::create([
            'actor_id' => $this->admin->id,
            'performed_by' => $this->admin->name,
            'target_user_id' => '1',
            'target_user_name' => 'Item A',
            'action' => LogService::ACTION_CREATED,
            'details' => 'Created item',
            'type' => 'activity'
        ]);

        ActivityLog::create([
            'actor_id' => $this->admin->id,
            'performed_by' => $this->admin->name,
            'target_user_id' => '2',
            'target_user_name' => 'Item B',
            'action' => LogService::ACTION_DELETED,
            'details' => 'Deleted item',
            'type' => 'activity'
        ]);

        $response = $this->actingAs($this->admin)
            ->getJson('/api/v1/activity-logs?action=' . LogService::ACTION_CREATED);

        $response->assertStatus(200);
        $data = $response->json('data.data');
        $this->assertCount(1, $data);
        $this->assertEquals(LogService::ACTION_CREATED, $data[0]['action']);
    }

    public function test_transaction_logs_can_be_filtered_by_action()
    {
        ActivityLog::create([
            'actor_id' => $this->admin->id,
            'performed_by' => $this->admin->name,
            'target_user_id' => '1',
            'target_user_name' => 'Student A',
            'action' => LogService::ACTION_BORROWED,
            'details' => 'Borrowed items: Laptop (1)',
            'type' => 'transaction'
        ]);

        ActivityLog::create([
            'actor_id' => $this->admin->id,
            'performed_by' => $this->admin->name,
            'target_user_id' => '1',
            'target_user_name' => 'Student A',
            'action' => LogService::ACTION_RETURNED,
            'details' => 'Returned items: Laptop (1)',
            'type' => 'transaction'
        ]);

        $response = $this->actingAs($this->admin)
            ->getJson('/api/v1/transaction-logs?action=' . LogService::ACTION_RETURNED);

        $response->assertStatus(200);
        $data = $response->json('data.data');
        $this->assertCount(1, $data);
        $this->assertEquals(LogService::ACTION_RETURNED, $data[0]['action']);
    }

    public function test_activity_logs_filter_rejects_invalid_action()
    {
        $response = $this->actingAs($this->admin)
            ->getJson('/api/v1/activity-logs?action=invalid_action');

        $response->assertStatus(422);
    }

    public function test_transaction_logs_filter_rejects_invalid_action()
    {
        $response = $this->actingAs($this->admin)
            ->getJson('/api/v1/transaction-logs?action=Items+Borrowed');

        $response->assertStatus(422);
    }
}
