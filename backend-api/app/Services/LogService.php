<?php

namespace App\Services;

use App\Models\ActivityLog;
use Illuminate\Support\Facades\Auth;

class LogService
{
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
        $query = ActivityLog::where('type', 'activity')->orderBy('created_at', 'desc');

        if (isset($filters['search'])) {
            $search = $filters['search'];
            $query->where(function($q) use ($search) {
                $q->where('action', 'like', "%{$search}%")
                  ->orWhere('details', 'like', "%{$search}%")
                  ->orWhere('performed_by', 'like', "%{$search}%")
                  ->orWhere('target_user_name', 'like', "%{$search}%");
            });
        }
        
        if (isset($filters['action'])) {
             $query->where('action', $filters['action']);
        }

        return $query->paginate($filters['per_page'] ?? 15);
    }

    public function getTransactionLogs(array $filters = [])
    {
        $query = ActivityLog::where('type', 'transaction')->orderBy('created_at', 'desc');

        if (isset($filters['search'])) {
            $search = $filters['search'];
            $query->where(function($q) use ($search) {
                $q->where('action', 'like', "%{$search}%")
                  ->orWhere('details', 'like', "%{$search}%")
                  ->orWhere('performed_by', 'like', "%{$search}%")
                  ->orWhere('target_user_name', 'like', "%{$search}%");
            });
        }
        
        if (isset($filters['action'])) {
             $query->where('action', $filters['action']);
        }

        return $query->paginate($filters['per_page'] ?? 15);
    }
}
