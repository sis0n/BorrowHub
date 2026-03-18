<?php

namespace Database\Seeders;

use App\Models\BorrowRecord;
use App\Models\Item;
use App\Models\Student;
use App\Models\User;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class BorrowRecordSeeder extends Seeder
{
    public function run(): void
    {
        $students = Student::all();
        $items = Item::where('available_quantity', '>', 0)->get();
        $admin = User::first();

        if ($students->isEmpty() || $items->isEmpty() || !$admin) {
            // Ensuring we have base data to create borrow records
            $this->call([
                UserSeeder::class,
                StudentSeeder::class,
                ItemSeeder::class,
            ]);
            $students = Student::all();
            $items = Item::where('available_quantity', '>', 0)->get();
            $admin = User::first();
        }

        for ($i = 0; $i < 20; $i++) {
            DB::transaction(function () use ($students, $items, $admin) {
                $student = $students->random();

                $availableItems = $items->where('available_quantity', '>', 0);

                if ($availableItems->isEmpty()) {
                    return; // Skip this iteration if no items are available
                }

                $borrowedItems = $availableItems->random(rand(1, min(3, $availableItems->count())));

                $borrowRecord = BorrowRecord::factory()->create([
                    'student_id' => $student->id,
                    'staff_id' => $admin->id,
                ]);

                foreach ($borrowedItems as $item) {
                    $borrowRecord->items()->attach($item->id, ['quantity' => 1]);
                    $item->decrement('available_quantity');
                }
            });
        }
    }
}
