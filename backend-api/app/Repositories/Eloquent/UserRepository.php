<?php

namespace App\Repositories\Eloquent;

use App\Models\User;
use App\Repositories\Interfaces\UserRepositoryInterface;

class UserRepository implements UserRepositoryInterface
{
    /**
     * Find a user by their username.
     *
     * @param string $username
     * @return \App\Models\User|null
     */
    public function findByUsername(string $username)
    {
        return User::where('username', $username)->first();
    }
}
