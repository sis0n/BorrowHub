<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class UserManagementTest extends TestCase
{
    use RefreshDatabase;

    protected $admin;
    protected $staff;

    protected function setUp(): void
    {
        parent::setUp();
        
        $this->admin = User::factory()->create([
            'username' => 'admin',
            'role' => 'admin'
        ]);

        $this->staff = User::factory()->create(['role' => 'staff']);
    }

    public function test_staff_cannot_access_user_management()
    {
        $response = $this->actingAs($this->staff)
                         ->getJson('/api/v1/users');

        $response->assertStatus(403);
    }

    public function test_admin_can_list_users()
    {
        User::factory()->count(3)->create();

        $response = $this->actingAs($this->admin)
                         ->getJson('/api/v1/users');

        $response->assertStatus(200)
                 ->assertJsonCount(5, 'data'); // 3 factory + 1 admin + 1 staff
    }

    public function test_admin_can_create_user()
    {
        $data = [
            'name' => 'New User',
            'username' => 'newuser',
            'password' => 'password123',
            'role' => 'staff'
        ];

        $response = $this->actingAs($this->admin)
                         ->postJson('/api/v1/users', $data);

        $response->assertStatus(201)
                 ->assertJsonPath('data.username', 'newuser');

        $this->assertDatabaseHas('users', ['username' => 'newuser']);
    }

    public function test_admin_can_update_user()
    {
        $user = User::factory()->create(['name' => 'Old Name']);

        $response = $this->actingAs($this->admin)
                         ->putJson("/api/v1/users/{$user->id}", [
                             'name' => 'Updated Name'
                         ]);

        $response->assertStatus(200)
                 ->assertJsonPath('data.name', 'Updated Name');
    }

    public function test_admin_cannot_delete_primary_administrator()
    {
        $response = $this->actingAs($this->admin)
                         ->deleteJson("/api/v1/users/{$this->admin->id}");

        $response->assertStatus(422);
        $this->assertDatabaseHas('users', ['id' => $this->admin->id]);
    }

    public function test_admin_can_delete_staff()
    {
        $user = User::factory()->create(['role' => 'staff']);

        $response = $this->actingAs($this->admin)
                         ->deleteJson("/api/v1/users/{$user->id}");

        $response->assertStatus(200);
        $this->assertDatabaseMissing('users', ['id' => $user->id]);
    }

    public function test_admin_can_reset_user_password()
    {
        $user = User::factory()->create();

        $response = $this->actingAs($this->admin)
                         ->postJson("/api/v1/users/{$user->id}/reset-password");

        $response->assertStatus(200);
    }
}
