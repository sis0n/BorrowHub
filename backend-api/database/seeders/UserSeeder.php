<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\User;
use Illuminate\Support\Facades\Hash;

class UserSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // Default Admin Account
        User::firstOrCreate(
            ['username' => 'admin'],
            [
                'name' => 'System Administrator',
                'password' => Hash::make('admin123'),
                'role' => 'admin',
            ]
        );

        // Default Staff Account
        User::firstOrCreate(
            ['username' => 'staff'],
            [
                'name' => 'CSD Staff',
                'password' => Hash::make('staff123'),
                'role' => 'staff',
            ]
        );
    }
}
