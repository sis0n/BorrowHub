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

    /**
     * Get only dashboard statistics summary.
     */
    public function stats(): JsonResponse
    {
        try {
            $stats = $this->dashboardService->getDashboardStatsSummary();

            return $this->successResponse(
                $stats,
                'Dashboard summary retrieved successfully.'
            );
        } catch (\Exception $e) {
            \Illuminate\Support\Facades\Log::error('Failed to retrieve dashboard summary.', ['exception' => $e]);
            return $this->errorResponse('An error occurred while retrieving dashboard summary.', 500);
        }
    }

    /**
     * Get only recent transactions.
     */
    public function recentTransactions(): JsonResponse
    {
        try {
            $transactions = $this->dashboardService->getRecentTransactions();

            return $this->successResponse(
                \App\Http\Resources\BorrowRecordResource::collection($transactions),
                'Recent transactions retrieved successfully.'
            );
        } catch (\Exception $e) {
            \Illuminate\Support\Facades\Log::error('Failed to retrieve recent transactions.', ['exception' => $e]);
            return $this->errorResponse('An error occurred while retrieving recent transactions.', 500);
        }
    }
}
