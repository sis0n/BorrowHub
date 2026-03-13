<?php

namespace App\Repositories\Interfaces;

interface ItemRepositoryInterface
{
    public function getAll(array $filters = []);
    public function findById($id, bool $lock = false);
    public function create(array $data);
    public function update($id, array $data);
    public function delete($id);
    public function decrementAvailableQuantity(int $id, int $quantity);
    public function incrementAvailableQuantity(int $id, int $quantity);
}