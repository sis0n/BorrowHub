<?php

namespace App\Repositories\Eloquent;

use App\Models\Item;
use App\Repositories\Interfaces\ItemRepositoryInterface;

class ItemRepository implements ItemRepositoryInterface
{
    public function getAll(array $filters = [])
    {
        $query = Item::with('category');

        if (isset($filters['search'])) {
            $query->where('name', 'like', '%' . $filters['search'] . '%');
        }

        if (isset($filters['category_id'])) {
            $query->where('category_id', $filters['category_id']);
        }

        if(isset($filters['status'])) {
            $query->where('status', $filters['status']);
        }

        $query->orderBy('name', 'asc');

        $perPage = isset($filters['per_page']) ? (int)$filters['per_page'] : config('borrow.pagination_size', 15);
        return $query->paginate($perPage);
    }

    public function findById($id, bool $lock = false)
    {
        $query = Item::with('category');

        if($lock) {
            $query->lockForUpdate();
        }

        return $query->findOrFail($id);
    }

    public function create(array $data)
    {
        $item = Item::create($data);
        return $item->load('category');
    }

    public function update($id, array $data)
    {
        $item = $this->findById($id);
        $item->update($data);
        return $item;
    }

    public function delete($id)
    {
        $item = $this->findById($id);
        return $item->delete();
    }

    public function decrementAvailableQuantity(\App\Models\Item $item, int $quantity)
    {
        $item->decrement('available_quantity', $quantity);
        return $item;
    }

    public function incrementAvailableQuantity(\App\Models\Item $item, int $quantity)
    {
        $item->increment('available_quantity', $quantity);
        return $item;
    }
}
