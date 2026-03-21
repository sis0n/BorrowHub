<?php

namespace App\Services;

use App\Repositories\Interfaces\DashboardRepositoryInterface;

class DashboardService
{
    protected $dashboardRepository;

    public function __construct(DashboardRepositoryInterface $dashboardRepository)
    {
        $this->dashboardRepository = $dashboardRepository;
    }

    /**
     * Get the dashboard statistics.
     *
     * @return array
     */
    public function getDashboardStats(): array
    {
        $stats = $this->getDashboardStatsSummary();
        $recentTransactions = $this->getRecentTransactions(5);

        return array_merge($stats, [
            'recent_transactions' => \App\Http\Resources\BorrowRecordResource::collection($recentTransactions)
        ]);
    }

    /**
     * Get only the statistics summary.
     *
     * @return array
     */
    public function getDashboardStatsSummary(): array
    {
        $totalItems = $this->dashboardRepository->getTotalItems();
        $availableNow = $this->dashboardRepository->getAvailableItems();
        $currentlyBorrowed = $this->dashboardRepository->getCurrentlyBorrowedCount();
        $dueTodayCount = $this->dashboardRepository->getDueTodayCount();

        return [
            'total_items' => $totalItems,
            'currently_borrowed' => $currentlyBorrowed,
            'available_now' => $availableNow,
            'due_today' => $dueTodayCount,
        ];
    }

    /**
     * Get recent transactions.
     *
     * @param int $limit
     * @return mixed
     */
    public function getRecentTransactions(int $limit = 5)
    {
        return $this->dashboardRepository->getRecentTransactions($limit);
    }
}
