<?php

namespace App\Services;

use App\Repositories\Interfaces\ItemRepositoryInterface;

class ItemService
{
    protected $itemRepository;

    public function __construct(ItemRepositoryInterface $itemRepository)
    {
        $this->itemRepository = $itemRepository;
    }

    public function getAllItems(array $filters = [])
    {
        return $this->itemRepository->getAll($filters);
    }

    public function createItem(array $data)
    {
        if(!isset($data['available_quantity'])) {
            $data['available_quantity'] = $data['total_quantity'];
        }

        return $this->itemRepository->create($data);
    }

    public function updateItem($id, array $data)
    {
        return $this->itemRepository->update($id, $data);
    }

    public function deleteItem($id)
    {
        return $this->itemRepository->delete($id);
    }

    public function getItemById($id)
    {
        return $this->itemRepository->findById($id);
    }
}