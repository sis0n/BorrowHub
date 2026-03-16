<?php

namespace App\Services;

use App\Repositories\Interfaces\StudentRepositoryInterface;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class StudentService
{
    protected $studentRepository;

    public function __construct(StudentRepositoryInterface $studentRepository)
    {
        $this->studentRepository = $studentRepository;
    }

    public function getAllStudents(array $filters)
    {
        return $this->studentRepository->getAll($filters);
    }

    public function getStudent(int $id)
    {
        return $this->studentRepository->findById($id);
    }

    public function getStudentByStudentNumber(string $studentNumber)
    {
        $student = $this->studentRepository->findByStudentNumber($studentNumber);
        if (!$student) {
            throw new \Illuminate\Database\Eloquent\ModelNotFoundException("Student with student number {$studentNumber} not found.");
        }
        return $student;
    }

    public function createStudent(array $data)
    {
        return $this->studentRepository->create($data);
    }

    public function updateStudent(int $id, array $data)
    {
        return $this->studentRepository->update($id, $data);
    }

    public function deleteStudent(int $id)
    {
        return $this->studentRepository->delete($id);
    }

    public function importStudents(UploadedFile $file)
    {
        $handle = fopen($file->getRealPath(), 'r');
        fgetcsv($handle);

        $results = [
            'success' => 0,
            'failed' => 0,
            'errors' => []
        ];

        $existingStudents = \App\Models\Student::pluck('student_number')->toArray();
        $existingStudentsMap = array_flip($existingStudents);

        DB::beginTransaction();
        try {
            $rowNumber = 1;
            while (($row = fgetcsv($handle)) !== false) {
                $rowNumber++;
                if (count($row) < 3) continue;

                $data = [
                    'student_number' => trim($row[0]),
                    'name' => trim($row[1]),
                    'course_id' => trim($row[2])
                ];

                try {
                    $validator = \Illuminate\Support\Facades\Validator::make($data, [
                        'student_number' => 'required|string',
                        'name' => 'required|string|max:255',
                        'course_id' => 'required|integer|exists:courses,id',
                    ]);

                    if ($validator->fails()) {
                        throw new \Exception(implode(' ', $validator->errors()->all()));
                    }

                    if (isset($existingStudentsMap[$data['student_number']])) {
                        throw new \Exception("Student number {$data['student_number']} already exists.");
                    }

                    $this->studentRepository->create($data);

                    $existingStudentsMap[$data['student_number']] = true;
                    $results['success']++;
                } catch (\Exception $e) {
                    $results['failed']++;
                    $results['errors'][] = "Row {$rowNumber}: {$e->getMessage()}";
                }
            }
            DB::commit();
        } catch (\Exception $e) {
            DB::rollBack();
            throw $e;
        } finally {
            fclose($handle);
        }

        return $results;
    }
}
