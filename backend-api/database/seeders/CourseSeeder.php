<?php

namespace Database\Seeders;

use App\Models\Course;
use Illuminate\Database\Seeder;

class CourseSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $courses = [
            'Bachelor of Science in Accountancy',
            'Bachelor of Science in Accounting Information System',
            'Bachelor of Science in Business Administration, Major in Financial Management',
            'Bachelor of Science in Business Administration, Major in Human Resource Management',
            'Bachelor of Science in Business Administration, Major in Marketing Management',
            'Bachelor of Science in Entrepreneurship',
            'Bachelor of Science in Hospitality Management',
            'Bachelor of Science in Office Administration',
            'Bachelor of Science in Tourism Management',
            'Bachelor of Arts in Political Science',
            'Bachelor of Arts in Communication',
            'Bachelor of Public Administration',
            'Bachelor of Public Administration (Special Program)',
            'Bachelor of Science in Computer Science',
            'Bachelor of Science in Entertainment and Multimedia Computing',
            'Bachelor of Science in Information System',
            'Bachelor of Science in Information Technology',
            'Bachelor of Science in Mathematics',
            'Bachelor of Science in Psychology',
            'Bachelor in Secondary Education, Major in English',
            'Bachelor in Secondary Education, Major in English – Chinese',
            'Bachelor in Secondary Education, Major in Science',
            'Bachelor in Secondary Education, Major in Technology and Livelihood Education',
            'Bachelor of Early Childhood Education',
            'Certificate in Professional Education (Elementary / Secondary / Physical Education)',
            'Bachelor of Science in Criminology',
            'Bachelor of Science in Industrial Security Management',
            'Bachelor of Science in Computer Engineering',
            'Bachelor of Science in Electrical Engineering',
            'Bachelor of Science in Electronics Engineering',
            'Bachelor of Science in Industrial Engineering',
            'Juris Doctor',
            'Doctor in Public Administration',
            'Doctor of Philosophy, Major in Educational Management',
            'Master in Public Administration',
            'Master of Arts in Education, Major in Educational Management',
            'Master of Arts in Education, Major in Teaching in the Early Grades',
            'Master of Arts in Education, Major in Teaching Science',
            'Master of Business Administration',
            'Master of Science in Criminal Justice, Major in Criminology',
            'Bachelor of Science in Social Work',
            'Bachelor of Science in Nursing',
            'Bachelor of Science in Medical Technology',
            'Bachelor of Science in Pharmacy',
            'Bachelor of Science in Midwifery',
        ];

        $now = now();
        $data = collect($courses)->unique()->map(function ($courseName) use ($now) {
            return [
                'name' => $courseName,
                'created_at' => $now,
                'updated_at' => $now,
            ];
        })->toArray();

        Course::insert($data);
    }
}
