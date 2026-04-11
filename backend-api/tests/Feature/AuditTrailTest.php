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
            'actor_id'       => $this->admin->id,
            'target_user_id' => (string) $this->admin->id,
            'target_type'    => 'user',
            'action'         => LogService::ACTION_CREATED,
            'details'        => 'Test Details',
            'type'           => 'activity',
        ]);

        $response = $this->actingAs($this->admin)->getJson('/api/v1/activity-logs');

        $response->assertStatus(200)
            ->assertJsonStructure([
                'status',
                'message',
                'data' => [
                    'data' => [
                        '*' => [
                            'id', 'actor_id', 'performed_by', 'target_user_id', 'target_user_name',
                            'action', 'details', 'created_at',
                        ],
                    ],
                ],
            ]);
    }

    public function test_admin_can_view_transaction_logs()
    {
        ActivityLog::create([
            'actor_id'       => $this->admin->id,
            'target_user_id' => '1',
            'target_type'    => 'student',
            'action'         => LogService::ACTION_BORROWED,
            'details'        => 'Borrowed items: Laptop (1)',
            'type'           => 'transaction',
        ]);

        $response = $this->actingAs($this->admin)->getJson('/api/v1/transaction-logs');

        $response->assertStatus(200)
             ->assertJsonFragment(['action' => LogService::ACTION_BORROWED]);
    }

    public function test_user_creation_logs_activity()
    {
        $response = $this->actingAs($this->admin)->postJson('/api/v1/users', [
            'name'     => 'New User',
            'username' => 'newuser',
            'password' => 'password',
            'role'     => 'staff',
        ]);

        $response->assertStatus(201);

        $this->assertDatabaseHas('activity_logs', [
            'action'      => LogService::ACTION_CREATED,
            'actor_id'    => $this->admin->id,
            'target_type' => 'user',
        ]);
    }

    public function test_item_creation_logs_activity()
    {
        $category = \App\Models\Category::factory()->create();

        $response = $this->actingAs($this->admin)->postJson('/api/v1/items', [
            'code'             => 'ITEM001',
            'name'             => 'Test Item',
            'category_id'      => $category->id,
            'total_quantity'   => 10,
            'description'      => 'Test Desc',
            'status'           => 'active',
        ]);

        $response->assertStatus(201);

        $this->assertDatabaseHas('activity_logs', [
            'action'      => LogService::ACTION_CREATED,
            'target_type' => 'item',
        ]);
    }

    public function test_activity_logs_can_be_filtered_by_action()
    {
        ActivityLog::create([
            'actor_id'       => $this->admin->id,
            'target_user_id' => '1',
            'target_type'    => 'item',
            'action'         => LogService::ACTION_CREATED,
            'details'        => 'Created item',
            'type'           => 'activity',
        ]);

        ActivityLog::create([
            'actor_id'       => $this->admin->id,
            'target_user_id' => '2',
            'target_type'    => 'item',
            'action'         => LogService::ACTION_DELETED,
            'details'        => 'Deleted item',
            'type'           => 'activity',
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
            'actor_id'       => $this->admin->id,
            'target_user_id' => '1',
            'target_type'    => 'student',
            'action'         => LogService::ACTION_BORROWED,
            'details'        => 'Borrowed items: Laptop (1)',
            'type'           => 'transaction',
        ]);

        ActivityLog::create([
            'actor_id'       => $this->admin->id,
            'target_user_id' => '1',
            'target_type'    => 'student',
            'action'         => LogService::ACTION_RETURNED,
            'details'        => 'Returned items: Laptop (1)',
            'type'           => 'transaction',
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
            ->getJson('/api/v1/transaction-logs?action=invalid_action');

        $response->assertStatus(422);
    }

    public function test_log_shows_current_actor_name_after_update()
    {
        $actor = User::factory()->create(['name' => 'Original Name', 'role' => 'staff']);

        ActivityLog::create([
            'actor_id'       => $actor->id,
            'target_user_id' => (string) $this->admin->id,
            'target_type'    => 'user',
            'action'         => LogService::ACTION_CREATED,
            'details'        => 'Test log entry',
            'type'           => 'activity',
        ]);

        // Rename the actor after the log was created
        $actor->update(['name' => 'Updated Name']);

        $response = $this->actingAs($this->admin)->getJson('/api/v1/activity-logs');

        $response->assertStatus(200);
        $logEntry = collect($response->json('data.data'))
            ->firstWhere('actor_id', $actor->id);

        $this->assertNotNull($logEntry);
        $this->assertEquals('Updated Name', $logEntry['performed_by']);
    }

    public function test_log_shows_current_target_name_after_update()
    {
        $course = \App\Models\Course::factory()->create();
        $student = Student::factory()->create(['name' => 'Original Student', 'course_id' => $course->id]);

        ActivityLog::create([
            'actor_id'       => $this->admin->id,
            'target_user_id' => (string) $student->id,
            'target_type'    => 'student',
            'action'         => LogService::ACTION_BORROWED,
            'details'        => 'Borrowed items: Laptop (1)',
            'type'           => 'transaction',
        ]);

        // Rename the student after the log was created
        $student->update(['name' => 'Renamed Student']);

        $response = $this->actingAs($this->admin)->getJson('/api/v1/transaction-logs');

        $response->assertStatus(200);
        $logEntry = collect($response->json('data.data'))
            ->firstWhere('target_user_id', (string) $student->id);

        $this->assertNotNull($logEntry);
        $this->assertEquals('Renamed Student', $logEntry['target_user_name']);
    }

    public function test_log_shows_actor_name_after_soft_delete()
    {
        $actor = User::factory()->create(['name' => 'Soon Deleted Actor', 'role' => 'staff']);

        ActivityLog::create([
            'actor_id'       => $actor->id,
            'target_user_id' => (string) $this->admin->id,
            'target_type'    => 'user',
            'action'         => LogService::ACTION_CREATED,
            'details'        => 'Test log entry',
            'type'           => 'activity',
        ]);

        // Soft-delete the actor
        $actor->delete();
        $this->assertSoftDeleted($actor);

        $response = $this->actingAs($this->admin)->getJson('/api/v1/activity-logs');

        $response->assertStatus(200);
        $logEntry = collect($response->json('data.data'))
            ->firstWhere('actor_id', $actor->id);

        $this->assertNotNull($logEntry);
        $this->assertEquals('Soon Deleted Actor', $logEntry['performed_by']);
    }

    public function test_log_shows_target_student_name_after_soft_delete()
    {
        $course = \App\Models\Course::factory()->create();
        $student = Student::factory()->create(['name' => 'Soon Deleted Student', 'course_id' => $course->id]);

        ActivityLog::create([
            'actor_id'       => $this->admin->id,
            'target_user_id' => (string) $student->id,
            'target_type'    => 'student',
            'action'         => LogService::ACTION_BORROWED,
            'details'        => 'Borrowed items: Laptop (1)',
            'type'           => 'transaction',
        ]);

        // Soft-delete the student
        $student->delete();
        $this->assertSoftDeleted($student);

        $response = $this->actingAs($this->admin)->getJson('/api/v1/transaction-logs');

        $response->assertStatus(200);
        $logEntry = collect($response->json('data.data'))
            ->firstWhere('target_user_id', (string) $student->id);

        $this->assertNotNull($logEntry);
        $this->assertEquals('Soon Deleted Student', $logEntry['target_user_name']);
    }
}

