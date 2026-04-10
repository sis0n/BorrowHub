<?php

namespace App\Services;

use App\Models\ActivityLog;
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

    public function log(string $action, string $details, string $targetId, string $targetName, string $type = 'activity')
    {
        $user = Auth::user();

        return ActivityLog::create([
            'actor_id' => $user ? $user->id : null,
            'performed_by' => $user ? $user->name : 'System',
            'target_user_id' => $targetId,
            'target_user_name' => $targetName,
            'action' => $action,
            'details' => $details,
            'type' => $type,
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
        $query = ActivityLog::where('type', $type)->orderBy('created_at', 'desc');

        if (isset($filters['search'])) {
            $search = $filters['search'];
            $query->where(function($q) use ($search) {
                $q->where('action', 'like', "%{$search}%")
                  ->orWhere('details', 'like', "%{$search}%")
                  ->orWhere('performed_by', 'like', "%{$search}%")
                  ->orWhere('target_user_name', 'like', "%{$search}%");
            });
        }
        
        if (isset($filters['action']) && $filters['action'] !== '') {
            $query->where('action', strtolower($filters['action']));
        }

        return $query->paginate($filters['per_page'] ?? 15);
    }
}
