<?php

namespace Tests\Feature;

use App\Models\Category;
use App\Models\Item;
use App\Models\Student;
use App\Models\User;
use App\Models\BorrowRecord;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class ReturnTransactionTest extends TestCase
{
    use RefreshDatabase;

    protected $staff;
    protected $student;
    protected $item;
    protected $borrowRecord;

    protected function setUp(): void
    {
        parent::setUp();

        $this->staff = User::factory()->create(['role' => 'staff']);
        $this->student = Student::factory()->create(['student_number' => '2023-00001']);

        $category = Category::factory()->create();
        $this->item = Item::factory()->create([
            'category_id' => $category->id,
            'total_quantity' => 10,
            'available_quantity' => 8,
            'status' => 'active'
        ]);

        $this->borrowRecord = BorrowRecord::create([
            'student_id' => $this->student->id,
            'staff_id' => $this->staff->id,
            'collateral' => 'Student ID',
            'borrowed_at' => now(),
            'due_at' => now()->addHours(2),
            'status' => 'borrowed'
        ]);

        $this->borrowRecord->items()->attach($this->item->id, ['quantity' => 2]);
    }

    public function test_can_search_active_transactions()
    {
        $response = $this->actingAs($this->staff)
            ->getJson("/api/v1/transactions/active?student_number=2023-00001");

        $response->assertStatus(200)
            ->assertJsonCount(1, 'data');
    }

    public function test_can_process_return_transaction()
    {
        $response = $this->actingAs($this->staff)
            ->postJson("/api/v1/transactions/{$this->borrowRecord->id}/return");

        $response->assertStatus(200)
            ->assertJsonPath('data.status', 'returned');

        $this->item->refresh();
        $this->assertEquals(10, $this->item->available_quantity);

        $this->assertDatabaseHas('borrow_records', [
            'id' => $this->borrowRecord->id,
            'status' => 'returned'
        ]);
    }

    public function test_cannot_return_already_returned_item()
    {
        $this->borrowRecord->update(['status' => 'returned']);

        $response = $this->actingAs($this->staff)
            ->postJson("/api/v1/transactions/{$this->borrowRecord->id}/return");

        $response->assertStatus(422);
    }
}
