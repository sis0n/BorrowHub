<?php

namespace App\Repositories\Interfaces;

interface UserRepositoryInterface
{
    /**
     * Find a user by their username.
     *
     * @param string $username
     * @return \App\Models\User|null
     */
    public function findByUsername(string $username);
}
