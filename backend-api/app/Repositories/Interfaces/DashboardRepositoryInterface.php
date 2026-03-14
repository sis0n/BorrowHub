<?php

namespace App\Repositories\Interfaces;

interface DashboardRepositoryInterface
{
    public function getTotalItems(): int;
    public function getAvailableItems(): int;
    public function getCurrentlyBorrowedCount(): int;
    public function getDueTodayCount(): int;
    public function getRecentTransactions(int $limit = 5);
}
