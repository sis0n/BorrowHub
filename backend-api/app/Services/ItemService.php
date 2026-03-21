<?php

namespace App\Services;

use App\Repositories\Interfaces\ItemRepositoryInterface;

class ItemService
{
    protected $itemRepository;
    protected $logService;

    public function __construct(ItemRepositoryInterface $itemRepository, LogService $logService)
    {
        $this->itemRepository = $itemRepository;
        $this->logService = $logService;
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

        $item = $this->itemRepository->create($data);

        $this->logService->log(
            'Item Created',
            "Created item with quantity: {$item->total_quantity}",
            (string)$item->id,
            $item->name
        );

        return $item;
    }

    public function updateItem($id, array $data)
    {
        $item = $this->itemRepository->update($id, $data);

        $this->logService->log(
            'Item Updated',
            "Updated item fields: " . implode(', ', array_keys($data)),
            (string)$item->id,
            $item->name
        );

        return $item;
    }

    public function deleteItem($id)
    {
        $item = $this->itemRepository->findById($id);
        $result = $this->itemRepository->delete($id);

        if ($result && $item) {
            $this->logService->log(
                'Item Deleted',
                "Deleted item",
                (string)$item->id,
                $item->name
            );
        }

        return $result;
    }

    public function getItemById($id)
    {
        return $this->itemRepository->findById($id);
    }
}
