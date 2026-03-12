<?php

namespace Tests\Unit;

use Tests\TestCase;
use App\Models\User;
use App\Models\Course;
use App\Models\Student;
use App\Models\Category;
use App\Models\Item;
use App\Models\BorrowRecord;
use App\Models\ActivityLog;
use Illuminate\Foundation\Testing\RefreshDatabase;
// use PHPUnit\Framework\TestCase;

class ModelRelationshipTest extends TestCase
{
    use RefreshDatabase;

    public function test_course_has_many_students()
    {
        $course = Course::factory()->has(Student::factory()->count(3))->create();
        $this->assertCount(3, $course->students);
        $this->assertInstanceOf(Student::class, $course->students->first());
    }
    
    public function test_student_belongs_to_course()
    {
        $student = Student::factory()->create();
        $this->assertInstanceOf(Course::class, $student->course);
    }

    public function test_category_has_many_items()
    {
        $category = Category::factory()->has(Item::factory()->count(2))->create();
        $this->assertCount(2, $category->items);
    }

    public function test_borrow_record_belongs_to_student_and_staff()
    {
        $borrowRecord = BorrowRecord::factory()->create();
        $this->assertInstanceOf(Student::class, $borrowRecord->student);
        $this->assertInstanceOf(User::class, $borrowRecord->staff);
    }

    public function test_borrow_record_has_many_items_via_pivot()
    {
        $borrowRecord = BorrowRecord::factory()->create();
        $items = Item::factory()->count(2)->create();

        $borrowRecord->items()->attach($items, ['quantity' => 1]);

        $this->assertCount(2, $borrowRecord->items);
        $this->assertEquals(1, $borrowRecord->items()->first()->pivot->quantity);
    }

    public function test_user_has_many_borrow_records_and_activity_logs()
    {
        $user = User::factory()
            ->has(BorrowRecord::factory()->count(2), 'borrowRecords')
            ->has(ActivityLog::factory()->count(2), 'activityLogs')
            ->create();
        
        $this->assertCount(2, $user->borrowRecords);
        $this->assertCount(2, $user->activityLogs);
    }

    public function test_activity_log_belongs_to_actor()
    {
        $log = ActivityLog::factory()->create();
        $this->assertInstanceOf(User::class, $log->actor);
    }

    public function test_item_belongs_to_category()
    {
        $item = Item::factory()->create();
        $this->assertInstanceOf(Category::class, $item->category);
    }

    public function test_student_has_many_borrow_records()
    {
        $student = Student::factory()->has(BorrowRecord::factory()->count(2))->create();
        $this->assertCount(2, $student->borrowRecords);
    }

    public function test_item_has_many_borrow_records()
    {
        $item = Item::factory()->create();
        $record = BorrowRecord::factory()->create();
        $item->borrowRecords()->attach($record, ['quantity' => 1]);

        $this->assertCount(1, $item->borrowRecords);
        $this->assertInstanceOf(BorrowRecord::class, $item->borrowRecords->first());
    }
}
