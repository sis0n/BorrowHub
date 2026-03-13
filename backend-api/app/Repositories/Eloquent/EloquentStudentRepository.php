<?php

namespace App\Repositories\Eloquent;

use App\Models\Student;
use App\Repositories\Interfaces\StudentRepositoryInterface;

class EloquentStudentRepository implements StudentRepositoryInterface
{
    public function getAll(array $filters = [])
    {
        $query = Student::with('course');

        if (isset($filters['student_number'])) {
            $query->where('student_number', 'like', $filters['student_number'] . '%');
        }

        if (isset($filters['name'])) {
            $query->where('name', 'like', $filters['name'] . '%');
        }

        if (isset($filters['course_id'])) {
            $query->where('course_id', $filters['course_id']);
        }

        return $query->latest()->paginate(config('borrow.pagination_size', 15));
    }

    public function findByStudentNumber(string $studentNumber)
    {
        return Student::with('course')
            ->where('student_number', $studentNumber)
            ->first();
    }

    public function findById(int $id)
    {
        return Student::with('course')->findOrFail($id);
    }

    public function create(array $data)
    {
        return Student::create($data)->load('course');
    }

    public function update(int $id, array $data)
    {
        $student = $this->findById($id);
        $student->update($data);
        return $student;
    }

    public function delete(int $id)
    {
        $student = $this->findById($id);
        return $student->delete();
    }
}
