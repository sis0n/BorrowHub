<?php

namespace Database\Seeders;

use App\Models\Category;
use App\Models\Item;
use Illuminate\Database\Seeder;

class ItemSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $equipmentCategory = Category::where('name', 'Equipment')->first();
        $laptopCategory = Category::where('name', 'Laptop')->first();

        if (!$equipmentCategory || !$laptopCategory) {
            $this->call(CategorySeeder::class);
            $equipmentCategory = Category::where('name', 'Equipment')->first();
            $laptopCategory = Category::where('name', 'Laptop')->first();
        }

        // 1. Projector
        Item::create([
            'category_id' => $equipmentCategory->id,
            'name' => 'Projector',
            'total_quantity' => 10,
            'available_quantity' => 10,
            'status' => 'active',
        ]);

        // 2. Laptops (30x)
        for ($i = 1; $i <= 30; $i++) {
            Item::create([
                'category_id' => $laptopCategory->id,
                'name' => "Laptop $i",
                'total_quantity' => 1,
                'available_quantity' => 1,
                'status' => 'active',
            ]);
        }

        // 4. HDMI Cable
        Item::create([
            'category_id' => $equipmentCategory->id,
            'name' => 'HDMI Cable',
            'total_quantity' => 20,
            'available_quantity' => 20,
            'status' => 'active',
        ]);

        // 5. Extension Cable
        Item::create([
            'category_id' => $equipmentCategory->id,
            'name' => 'Extension Cable',
            'total_quantity' => 15,
            'available_quantity' => 15,
            'status' => 'active',
        ]);

        // 6. TV Remote (Haier)
        Item::create([
            'category_id' => $equipmentCategory->id,
            'name' => 'TV Remote (Haier)',
            'total_quantity' => 5,
            'available_quantity' => 5,
            'status' => 'active',
        ]);

        // 7. TV Remote (KKJBL)
        Item::create([
            'category_id' => $equipmentCategory->id,
            'name' => 'TV Remote (KKJBL)',
            'total_quantity' => 5,
            'available_quantity' => 5,
            'status' => 'active',
        ]);

        // 8. TV Remote (Sky)
        Item::create([
            'category_id' => $equipmentCategory->id,
            'name' => 'TV Remote (Sky)',
            'total_quantity' => 5,
            'available_quantity' => 5,
            'status' => 'active',
        ]);
    }
}
