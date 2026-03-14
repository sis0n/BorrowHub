<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Services\DashboardService;
use Illuminate\Http\JsonResponse;

class DashboardController extends Controller
{
    protected $dashboardService;

    public function __construct(DashboardService $dashboardService)
    {
        $this->dashboardService = $dashboardService;
    }

    /**
     * Get dashboard statistics.
     */
    public function index(): JsonResponse
    {
        try {
            $stats = $this->dashboardService->getDashboardStats();

            return $this->successResponse(
                $stats,
                'Dashboard statistics retrieved successfully.'
            );
        } catch (\Exception $e) {
            \Illuminate\Support\Facades\Log::error('Failed to retrieve dashboard statistics.', ['exception' => $e]);
            return $this->errorResponse('An error occurred while retrieving dashboard statistics.', 500);
        }
    }
}
