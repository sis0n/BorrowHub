<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class BorrowRecordResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'student' => new StudentResource($this->whenLoaded('student')),
            'staff' => new UserResource($this->whenLoaded('staff')),
            'collateral' => $this->collateral,
            'status' => $this->status,
            'borrowed_at' => $this->borrowed_at,
            'due_at' => $this->due_at,
            'returned_at' => $this->returned_at,
            'items' => ItemResource::collection($this->whenLoaded('items')),
            'created_at' => $this->created_at,
        ];
    }
}