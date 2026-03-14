<?php

namespace Tests\Feature;

use App\Models\Category;
use App\Models\Course;
use App\Models\Item;
use App\Models\Student;
use App\Models\User;
use App\Models\BorrowRecord;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Carbon;
use Tests\TestCase;

class DashboardTest extends TestCase
{
    use RefreshDatabase;

    private $user;
    private $category;
    private $course;
    private $student;

    protected function setUp(): void
    {
        parent::setUp();

        $this->user = User::factory()->create();

        $this->category = Category::factory()->create([
            'name' => 'Laptops',
        ]);

        $this->course = Course::factory()->create([
            'name' => 'BSCS',
        ]);

        $this->student = Student::factory()->create([
            'student_number' => '2023-00001',
            'name' => 'John Doe',
            'course_id' => $this->course->id,
        ]);
    }

    public function test_unauthenticated_user_cannot_access_dashboard()
    {
        $response = $this->getJson('/api/v1/dashboard');
        $response->assertStatus(401);
    }

    public function test_authenticated_user_can_access_dashboard_and_get_correct_stats()
    {
        // Create items
        $item1 = Item::factory()->create([
            'name' => 'Laptop A',
            'category_id' => $this->category->id,
            'total_quantity' => 10,
            'available_quantity' => 8, // 2 borrowed
            'status' => 'active',
        ]);

        $item2 = Item::factory()->create([
            'name' => 'Laptop B',
            'category_id' => $this->category->id,
            'total_quantity' => 5,
            'available_quantity' => 5, // 0 borrowed
            'status' => 'active',
        ]);

        // Create a borrow record
        $record = BorrowRecord::factory()->create([
            'student_id' => $this->student->id,
            'staff_id' => $this->user->id,
            'collateral' => 'Student ID',
            'borrowed_at' => Carbon::now(),
            'due_at' => Carbon::today()->setTime(20, 0, 0),
            'status' => 'borrowed',
        ]);

        $record->items()->attach($item1->id, ['quantity' => 2]);

        // Create another record due tomorrow
        $record2 = BorrowRecord::factory()->create([
            'student_id' => $this->student->id,
            'staff_id' => $this->user->id,
            'collateral' => 'Driver License',
            'borrowed_at' => Carbon::now(),
            'due_at' => Carbon::tomorrow()->setTime(20, 0, 0),
            'status' => 'borrowed',
        ]);
        $record2->items()->attach($item2->id, ['quantity' => 1]); // Adjust available later if needed but here we just test logic
        // For Dashboard logic currently: it joins borrow_record_items, doesn't validate item available_quantity strictly for stats

        $response = $this->actingAs($this->user)->getJson('/api/v1/dashboard');

        $response->assertStatus(200)
            ->assertJsonStructure([
                'status',
                'message',
                'data' => [
                    'total_items',
                    'currently_borrowed',
                    'available_now',
                    'due_today',
                    'recent_transactions' => [
                        '*' => [
                            'id',
                            'student' => ['id', 'name', 'student_number'],
                            'items' => [
                                '*' => ['id', 'name']
                            ],
                            'status',
                            'borrowed_at',
                            'due_at',
                        ]
                    ]
                ]
            ]);

        $data = $response->json('data');

        $this->assertEquals(15, $data['total_items']); // 10 + 5
        $this->assertEquals(13, $data['available_now']); // 8 + 5
        $this->assertEquals(3, $data['currently_borrowed']); // 2 from first record, 1 from second record
        $this->assertEquals(1, $data['due_today']); // only record 1 is due today
        $this->assertCount(2, $data['recent_transactions']);
    }
}
