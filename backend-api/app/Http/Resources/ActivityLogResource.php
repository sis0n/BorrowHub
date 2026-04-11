<?php

namespace App\Http\Resources;

use App\Models\Item;
use App\Models\Student;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class ActivityLogResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'id'               => $this->id,
            'actor_id'         => $this->actor_id,
            'performed_by'     => optional($this->actor)->name ?? 'System',
            'target_user_id'   => $this->target_user_id,
            'target_user_name' => $this->resolveTargetName(),
            'action'           => $this->action,
            'details'          => $this->details,
            'type'             => $this->type,
            'created_at'       => $this->created_at,
        ];
    }

    /**
     * Dynamically resolve the target's current name based on target_type.
     */
    private function resolveTargetName(): string
    {
        return match ($this->target_type) {
            'user'    => optional(User::withTrashed()->find($this->target_user_id))->name ?? 'Unknown',
            'student' => optional(Student::withTrashed()->find($this->target_user_id))->name ?? 'Unknown',
            'item'    => optional(Item::find($this->target_user_id))->name ?? 'Unknown',
            default   => $this->target_user_id ?? 'Unknown',
        };
    }
}
