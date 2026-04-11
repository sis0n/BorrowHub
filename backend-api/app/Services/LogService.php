<?php

namespace App\Services;

use App\Models\ActivityLog;
use App\Models\Item;
use App\Models\Student;
use App\Models\User;
use Illuminate\Support\Facades\Auth;

class LogService
{
    const ACTION_BORROWED = 'borrowed';
    const ACTION_RETURNED = 'returned';
    const ACTION_CREATED = 'created';
    const ACTION_UPDATED = 'updated';
    const ACTION_DELETED = 'deleted';

    const VALID_ACTIONS = [
        self::ACTION_BORROWED,
        self::ACTION_RETURNED,
        self::ACTION_CREATED,
        self::ACTION_UPDATED,
        self::ACTION_DELETED,
    ];

    public function log(string $action, string $details, string $targetId, string $targetType = 'user', string $type = 'activity')
    {
        return ActivityLog::create([
            'actor_id'       => Auth::id(),
            'target_user_id' => $targetId,
            'target_type'    => $targetType,
            'action'         => $action,
            'details'        => $details,
            'type'           => $type,
        ]);
    }

    public function getActivityLogs(array $filters = [])
    {
        return $this->getLogs('activity', $filters);
    }

    public function getTransactionLogs(array $filters = [])
    {
        return $this->getLogs('transaction', $filters);
    }

    private function getLogs(string $type, array $filters = [])
    {
        $query = ActivityLog::with('actor')->where('type', $type)->orderBy('created_at', 'desc');

        // Restrict Staff to their own activity logs; all users may see transaction logs
        if ($type === 'activity' && Auth::user()->role !== 'admin') {
            $query->where('actor_id', Auth::id());
        }

        if (isset($filters['search'])) {
            $search = $filters['search'];

            $userIds = User::withTrashed()
                ->where('name', 'like', "%{$search}%")
                ->pluck('id')
                ->map(fn($id) => (string) $id)
                ->all();

            $studentIds = Student::withTrashed()
                ->where('name', 'like', "%{$search}%")
                ->pluck('id')
                ->map(fn($id) => (string) $id)
                ->all();

            $itemIds = Item::where('name', 'like', "%{$search}%")
                ->pluck('id')
                ->map(fn($id) => (string) $id)
                ->all();

            $query->where(function ($q) use ($search, $userIds, $studentIds, $itemIds) {
                $q->where('action', 'like', "%{$search}%")
                  ->orWhere('details', 'like', "%{$search}%")
                  ->orWhereIn('actor_id', User::withTrashed()
                      ->where('name', 'like', "%{$search}%")
                      ->pluck('id'))
                  ->orWhere(function ($sq) use ($userIds) {
                      $sq->where('target_type', 'user')
                         ->whereIn('target_user_id', $userIds);
                  })
                  ->orWhere(function ($sq) use ($studentIds) {
                      $sq->where('target_type', 'student')
                         ->whereIn('target_user_id', $studentIds);
                  })
                  ->orWhere(function ($sq) use ($itemIds) {
                      $sq->where('target_type', 'item')
                         ->whereIn('target_user_id', $itemIds);
                  });
            });
        }

        if (isset($filters['action']) && $filters['action'] !== '') {
            $query->where('action', strtolower($filters['action']));
        }

        return $query->paginate($filters['per_page'] ?? 15);
    }
}

