<?php

namespace Database\Seeders;

use App\Models\Course;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class CourseSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $courses = [
            'BS in Information Technology',
            'BS in Computer Science',
            'BS in Information Systems',
        ];

        $data = [];
        $now = now();
        foreach ($courses as $courseName) {
            $data[] = [
                'name' => $courseName,
                'created_at' => $now,
                'updated_at' => $now,
            ];
        }
        Course::insert($data);
    }
}

