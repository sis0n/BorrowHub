<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
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
        $filters = $request->only(['search', 'action', 'per_page']);
        return $this->successResponse(
            $this->logService->getActivityLogs($filters),
            'Activity logs retrieved successfully.'
        );
    }

    public function indexTransactionLogs(Request $request)
    {
        $filters = $request->only(['search', 'action', 'per_page']);
        return $this->successResponse(
            $this->logService->getTransactionLogs($filters),
            'Transaction logs retrieved successfully.'
        );
    }
}
