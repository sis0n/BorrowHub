<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class AuthTest extends TestCase
{
    use RefreshDatabase;

    protected function setUp(): void
    {
        parent::setUp();

        User::create([
            'name' => 'Test Admin',
            'username' => 'testadmin',
            'password' => Hash::make('password123'),
            'role' => 'admin',
        ]);
    }

    /** @test */
    public function a_user_can_login_with_correct_credentials()
    {
        $response = $this->postJson('/api/v1/login', [
            'username' => 'testadmin',
            'password' => 'password123',
        ]);

        $response->assertStatus(200)
            ->assertJsonStructure([
                'status',
                'message',
                'data' => [
                    'user' => ['id', 'name', 'username', 'role'],
                    'token'
                ]
            ])
            ->assertJson([
                'status' => 'success',
                'data' => [
                    'user' => [
                        'username' => 'testadmin',
                        'role' => 'admin'
                    ]
                ]
            ]);
    }

    /** @test */
    public function a_user_cannot_login_with_incorrect_password()
    {
        $response = $this->postJson('/api/v1/login', [
            'username' => 'testadmin',
            'password' => 'wrong-password',
        ]);

        $response->assertStatus(401)
            ->assertJson([
                'status' => 'error',
            ]);
    }

    /** @test */
    public function a_user_can_retrieve_their_profile_when_authenticated()
    {
        $user = User::where('username', 'testadmin')->first();
        $token = $user->createToken('test-token')->plainTextToken;

        $response = $this->withHeaders([
            'Authorization' => 'Bearer ' . $token,
        ])->getJson('/api/v1/user');

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'data' => [
                    'username' => 'testadmin',
                ]
            ]);
    }

    /** @test */
    public function a_user_cannot_access_profile_without_authentication()
    {
        $response = $this->getJson('/api/v1/user');

        $response->assertStatus(401);
    }

    /** @test */
    public function a_user_can_logout()
    {
        $user = User::where('username', 'testadmin')->first();
        $token = $user->createToken('test-token')->plainTextToken;

        $response = $this->withHeaders([
            'Authorization' => 'Bearer ' . $token,
        ])->postJson('/api/v1/logout');

        $response->assertStatus(200)
            ->assertJson([
                'status' => 'success',
                'message' => 'User logged out successfully.',
            ]);

        $this->assertEquals(0, $user->tokens()->count());
    }
}
