<?php

namespace Database\Factories;

use App\Models\User;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends \Illuminate\Database\Eloquent\Factories\Factory<\App\Models\ActivityLog>
 */
class ActivityLogFactory extends Factory
{
    /**
     * Define the model's default state.
     *
     * @return array<string, mixed>
     */
    public function definition(): array
    {
        return [
            'actor_id'       => User::factory(),
            'target_user_id' => (string) fake()->randomNumber(5),
            'target_type'    => 'user',
            'action'         => 'created',
            'details'        => 'user logged into the system.',
            'type'           => 'activity',
        ];
    }
}
