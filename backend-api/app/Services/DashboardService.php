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
        $totalItems = $this->dashboardRepository->getTotalItems();
        $availableNow = $this->dashboardRepository->getAvailableItems();
        $currentlyBorrowed = $this->dashboardRepository->getCurrentlyBorrowedCount();
        $dueTodayCount = $this->dashboardRepository->getDueTodayCount();

        $recentTransactions = $this->dashboardRepository->getRecentTransactions(5);

        return [
            'total_items' => $totalItems,
            'currently_borrowed' => $currentlyBorrowed,
            'available_now' => $availableNow,
            'due_today' => $dueTodayCount,
            'recent_transactions' => \App\Http\Resources\BorrowRecordResource::collection($recentTransactions)
        ];
    }
}
