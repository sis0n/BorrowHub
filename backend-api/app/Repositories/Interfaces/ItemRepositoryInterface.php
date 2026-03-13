<?php

namespace App\Repositories\Interfaces;

use App\Models\Item;

interface ItemRepositoryInterface
{
    public function getAll(array $filters = []);
    public function findById($id, bool $lock = false);
    public function create(array $data);
    public function update($id, array $data);
    public function delete($id);
    public function decrementAvailableQuantity(Item $item, int $quantity);
    public function incrementAvailableQuantity(Item $item, int $quantity);
}
