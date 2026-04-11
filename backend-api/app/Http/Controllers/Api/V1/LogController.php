<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Http\Resources\ActivityLogResource;
use App\Services\LogService;
use Illuminate\Http\Request;

class LogController extends Controller
{
    protected $logService;

    public function __construct(LogService $logService)
    {
        $this->logService = $logService;
    }

    public function indexActivityLogs(Request $request)
    {
        $request->validate([
            'action' => ['nullable', 'string', 'in:' . implode(',', LogService::VALID_ACTIONS)],
        ]);

        $filters = $request->only(['search', 'action', 'per_page']);
        $logs = $this->logService->getActivityLogs($filters);
        $logs->getCollection()->transform(fn ($log) => (new ActivityLogResource($log))->toArray($request));

        return $this->successResponse($logs, 'Activity logs retrieved successfully.');
    }

    public function indexTransactionLogs(Request $request)
    {
        $request->validate([
            'action' => ['nullable', 'string', 'in:' . implode(',', LogService::VALID_ACTIONS)],
        ]);

        $filters = $request->only(['search', 'action', 'per_page']);
        $logs = $this->logService->getTransactionLogs($filters);
        $logs->getCollection()->transform(fn ($log) => (new ActivityLogResource($log))->toArray($request));

        return $this->successResponse($logs, 'Transaction logs retrieved successfully.');
    }
}
