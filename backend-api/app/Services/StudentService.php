<?php

namespace App\Services;

use App\Repositories\Interfaces\StudentRepositoryInterface;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class StudentService
{
    protected $studentRepository;
    protected $logService;

    public function __construct(StudentRepositoryInterface $studentRepository, LogService $logService)
    {
        $this->studentRepository = $studentRepository;
        $this->logService = $logService;
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
        $student = $this->studentRepository->create($data);

        $this->logService->log(
            'Student Created',
            "Created student in course ID: {$student->course_id}",
            (string)$student->id,
            $student->name
        );

        return $student;
    }

    public function updateStudent(int $id, array $data)
    {
        $student = $this->studentRepository->update($id, $data);

        $this->logService->log(
            'Student Updated',
            "Updated student fields: " . implode(', ', array_keys($data)),
            (string)$student->id,
            $student->name
        );

        return $student;
    }

    public function deleteStudent(int $id)
    {
        $student = $this->studentRepository->findById($id);
        $result = $this->studentRepository->delete($id);

        if ($result && $student) {
            $this->logService->log(
                'Student Deleted',
                "Deleted student",
                (string)$student->id,
                $student->name
            );
        }

        return $result;
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

            if ($results['success'] > 0) {
                $this->logService->log(
                    'Students Imported',
                    "Imported {$results['success']} students. Failed: {$results['failed']}",
                    "N/A",
                    "Batch Import"
                );
            }

        } catch (\Exception $e) {
            DB::rollBack();
            throw $e;
        } finally {
            fclose($handle);
        }

        return $results;
    }
}
