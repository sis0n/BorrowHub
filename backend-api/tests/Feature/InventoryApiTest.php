<?php

namespace Tests\Feature;

use App\Models\Category;
use App\Models\Item;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class InventoryApiTest extends TestCase
{
    use RefreshDatabase;

    protected $user;

    protected function setUp(): void
    {
        parent::setUp();
        // Gawa tayo ng staff user para sa authentication
        $this->user = User::factory()->create(['role' => 'staff']);
    }

    public function test_can_list_categories()
    {
        Category::factory()->count(3)->create();

        $response = $this->actingAs($this->user)
            ->getJson('/api/v1/categories');

        $response->assertStatus(200)
            ->assertJsonCount(3, 'data');
    }

    public function test_can_list_items()
    {
        Item::factory()->count(5)->create();

        $response = $this->actingAs($this->user)
            ->getJson('/api/v1/items');

        $response->assertStatus(200)
            ->assertJsonCount(5, 'data');
    }

    public function test_can_create_item()
    {
        $category = Category::factory()->create();

        $itemData = [
            'category_id' => $category->id,
            'name' => 'Test Item',
            'total_quantity' => 10,
            'status' => 'active'
        ];

        $response = $this->actingAs($this->user)
            ->postJson('/api/v1/items', $itemData);

        $response->assertStatus(201)
            ->assertJsonPath('data.name', 'Test Item');

        $this->assertDatabaseHas('items', ['name' => 'Test Item']);
    }

    public function test_can_update_item()
    {
        $item = Item::factory()->create(['name' => 'Old Name']);

        $response = $this->actingAs($this->user)
            ->putJson("/api/v1/items/{$item->id}", [
                'name' => 'New Name'
            ]);

        $response->assertStatus(200)
            ->assertJsonPath('data.name', 'New Name');
    }

    public function test_can_delete_item()
    {
        $item = Item::factory()->create();

        $response = $this->actingAs($this->user)
            ->deleteJson("/api/v1/items/{$item->id}");

        $response->assertStatus(200);
        $this->assertDatabaseMissing('items', ['id' => $item->id]);
    }
}
