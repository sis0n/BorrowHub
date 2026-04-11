<?php

namespace Tests\Feature;

use App\Models\Category;
use App\Models\Item;
use App\Models\Student;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class BorrowTransactionTest extends TestCase
{
    use RefreshDatabase;

    protected $staff;
    protected $student;
    protected $item;

    protected function setUp(): void
    {
        parent::setUp();

        $this->staff = User::factory()->create(['role' => 'staff']);
        $this->student = Student::factory()->create(['student_number' =>
        '2023-00001']);

        $category = Category::factory()->create();
        $this->item = Item::factory()->create([
            'category_id' => $category->id,
            'total_quantity' => 10,
            'available_quantity' => 10,
            'status' => 'active'
        ]);
    }

    public function test_can_lookup_student_by_number()
    {
        $response = $this->actingAs($this->staff)
            ->getJson("/api/v1/students/{$this->student->student_number}");

        $response->assertStatus(403);
    }

    public function test_can_process_borrow_transaction()
    {
        $payload = [
            'student_number' => $this->student->student_number,
            'collateral' => 'Student ID',
            'items' => [
                ['id' => $this->item->id, 'quantity' => 2]
            ]
        ];

        $response = $this->actingAs($this->staff)
            ->postJson('/api/v1/transactions/borrow', $payload);

        $response->assertStatus(201)
            ->assertJsonPath('data.student.course', $this->student->course->name);

        $this->assertDatabaseHas('borrow_records', [
            'student_id' => $this->student->id,
            'status' => 'borrowed'
        ]);

        $this->item->refresh();
        $this->assertEquals(8, $this->item->available_quantity);
    }

    public function test_cannot_borrow_with_insufficient_stock()
    {
        $payload = [
            'student_number' => $this->student->student_number,
            'collateral' => 'Student ID',
            'items' => [
                ['id' => $this->item->id, 'quantity' => 11]
            ]
        ];

        $response = $this->actingAs($this->staff)
            ->postJson(
                '/api/v1/transactions/borrow',
                $payload
            );

        $response->assertStatus(422);

        $this->item->refresh();
        $this->assertEquals(10, $this->item->available_quantity);
    }
}
