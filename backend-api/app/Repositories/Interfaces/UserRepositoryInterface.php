<?php

namespace App\Repositories\Interfaces;

interface UserRepositoryInterface
{
    public function getAll();
    public function findByUsername(string $username);
    public function findById(int $id);
    public function create(array $data);
    public function update(int $id, array $data);
    public function delete(int $id);
}
