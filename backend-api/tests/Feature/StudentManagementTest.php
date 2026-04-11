<?php

namespace Tests\Feature;

use App\Models\Course;
use App\Models\Student;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class StudentManagementTest extends TestCase
{
    use RefreshDatabase;

    protected $admin;
    protected $staff;
    protected $course;

    protected function setUp(): void
    {
        parent::setUp();
        $this->admin = User::factory()->create(['role' => 'admin']);
        $this->staff = User::factory()->create(['role' => 'staff']);
        $this->course = Course::create(['name' => 'BS Computer Science']);
    }

    public function test_staff_cannot_list_students()
    {
        $response = $this->actingAs($this->staff)
            ->getJson('/api/v1/students');

        $response->assertStatus(403);
    }

    public function test_staff_cannot_create_student()
    {
        $data = [
            'student_number' => '2023-9999',
            'name' => 'Unauthorized Student',
            'course_id' => $this->course->id,
        ];

        $response = $this->actingAs($this->staff)
            ->postJson('/api/v1/students', $data);

        $response->assertStatus(403);
    }

    public function test_staff_cannot_import_students()
    {
        // Empty payload is intentional; the role:admin middleware rejects the request
        // before any payload validation occurs.
        $response = $this->actingAs($this->staff)
            ->postJson('/api/v1/students/import', []);

        $response->assertStatus(403);
    }

    public function test_can_list_students_with_pagination()
    {
        Student::factory()->count(20)->create(['course_id' => $this->course->id]);

        $response = $this->actingAs($this->admin)
            ->getJson('/api/v1/students');

        $response->assertStatus(200)
            ->assertJsonCount(15, 'data'); 
    }

    public function test_can_search_students_by_student_number()
    {
        Student::factory()->create([
            'student_number' => '2023-0001',
            'name' => 'John Doe',
            'course_id' => $this->course->id
        ]);
        Student::factory()->create([
            'student_number' => '2023-0002',
            'name' => 'Jane Smith',
            'course_id' => $this->course->id
        ]);

        $response = $this->actingAs($this->admin)
            ->getJson('/api/v1/students?student_number=2023-0001');

        $response->assertStatus(200)
            ->assertJsonCount(1, 'data')
            ->assertJsonPath('data.0.student_number', '2023-0001')
            ->assertJsonPath('data.0.course', $this->course->name);
    }

    public function test_can_create_student()
    {
        $data = [
            'student_number' => '2023-1234',
            'name' => 'New Student',
            'course_id' => $this->course->id
        ];

        $response = $this->actingAs($this->admin)
            ->postJson('/api/v1/students', $data);

        $response->assertStatus(201)
            ->assertJsonPath('data.student_number', '2023-1234')
            ->assertJsonPath('data.course', $this->course->name);

        $this->assertDatabaseHas('students', $data);
    }

    public function test_cannot_create_student_with_duplicate_number()
    {
        Student::factory()->create(['student_number' => '2023-1234', 'course_id' => $this->course->id]);

        $data = [
            'student_number' => '2023-1234',
            'name' => 'Duplicate Student',
            'course_id' => $this->course->id
        ];

        $response = $this->actingAs($this->admin)
            ->postJson('/api/v1/students', $data);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['student_number']);
    }

    public function test_can_view_student_details()
    {
        $student = Student::factory()->create(['course_id' => $this->course->id]);

        $response = $this->actingAs($this->admin)
            ->getJson("/api/v1/students/{$student->id}");

        $response->assertStatus(200)
            ->assertJsonPath('data.id', $student->id)
            ->assertJsonPath('data.course', $this->course->name);
    }

    public function test_can_view_student_by_student_number()
    {
        $student = Student::factory()->create([
            'student_number' => '2023-0001',
            'course_id' => $this->course->id
        ]);

        $response = $this->actingAs($this->admin)
            ->getJson("/api/v1/students/2023-0001");

        $response->assertStatus(200)
            ->assertJsonPath('data.id', $student->id)
            ->assertJsonPath('data.course', $this->course->name);
    }

    public function test_can_update_student()
    {
        $student = Student::factory()->create(['course_id' => $this->course->id]);
        $updatedData = ['name' => 'Updated Name'];

        $response = $this->actingAs($this->admin)
            ->putJson("/api/v1/students/{$student->id}", $updatedData);

        $response->assertStatus(200)
            ->assertJsonPath('data.name', 'Updated Name')
            ->assertJsonPath('data.course', $this->course->name);

        $this->assertDatabaseHas('students', array_merge(['id' => $student->id], $updatedData));
    }

    public function test_can_delete_student()
    {
        $student = Student::factory()->create(['course_id' => $this->course->id]);

        $response = $this->actingAs($this->admin)
            ->deleteJson("/api/v1/students/{$student->id}");

        $response->assertStatus(200);
        $this->assertSoftDeleted('students', ['id' => $student->id]);
    }
}
