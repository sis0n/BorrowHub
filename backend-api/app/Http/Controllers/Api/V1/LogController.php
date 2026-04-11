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
        $this->validateLogFilters($request);

        $filters = $request->only(['search', 'action', 'per_page', 'start_date', 'end_date']);
        $logs = $this->logService->getActivityLogs($filters);
        $logs->getCollection()->transform(fn ($log) => (new ActivityLogResource($log))->toArray($request));

        return $this->successResponse($logs, 'Activity logs retrieved successfully.');
    }

    public function indexTransactionLogs(Request $request)
    {
        $this->validateLogFilters($request);

        $filters = $request->only(['search', 'action', 'per_page', 'start_date', 'end_date']);
        $logs = $this->logService->getTransactionLogs($filters);
        $logs->getCollection()->transform(fn ($log) => (new ActivityLogResource($log))->toArray($request));

        return $this->successResponse($logs, 'Transaction logs retrieved successfully.');
    }

    private function validateLogFilters(Request $request): void
    {
        $request->validate([
            'action'     => ['nullable', 'string', 'in:' . implode(',', LogService::VALID_ACTIONS)],
            'start_date' => ['nullable', 'date_format:Y-m-d'],
            'end_date'   => ['nullable', 'date_format:Y-m-d', function ($attribute, $value, $fail) use ($request) {
                if ($value && $request->input('start_date') && $value < $request->input('start_date')) {
                    $fail('The end date must be a date after or equal to start date.');
                }
            }],
        ]);
    }
}
