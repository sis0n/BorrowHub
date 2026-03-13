<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Http\Requests\Inventory\StoreItemRequest;
use App\Http\Requests\Inventory\UpdateItemRequest;
use App\Http\Resources\ItemResource;
use App\Services\ItemService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ItemController extends Controller
{
    protected $itemService;

    public function __construct(ItemService $itemService)
    {
        $this->itemService = $itemService;
    }

    public function index(Request $request): JsonResponse
    {
        $items = $this->itemService->getAllItems($request->all());

        return $this->successResponse(
            ItemResource::collection($items),
            'Items retrieved successfully'
        );
    }

    public function store(StoreItemRequest $request): JsonResponse
    {
        $item = $this->itemService->createItem($request->validated());

        return $this->successResponse(
            new ItemResource($item),
            'Item created successfully',
            201
        );
    }

    public function show($id): JsonResponse
    {
        $item = $this->itemService->getItemById($id);

        return $this->successResponse(
            new ItemResource($item),
            'Item retrieved successfully'
        );
    }

    public function update(UpdateItemRequest $request, $id): JsonResponse
    {
        $item = $this->itemService->updateItem($id, $request->validated());

        return $this->successResponse(
            new ItemResource($item),
            'Item updated successfully'
        );
    }

    public function destroy($id): JsonResponse
    {
        $this->itemService->deleteItem($id);

        return $this->successResponse(null, 'Item deleted successfully');
    }
}