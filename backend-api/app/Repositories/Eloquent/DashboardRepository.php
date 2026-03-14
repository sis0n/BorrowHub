<?php

namespace App\Repositories\Eloquent;

use App\Models\Item;
use App\Models\BorrowRecord;
use App\Repositories\Interfaces\DashboardRepositoryInterface;
use Illuminate\Support\Carbon;

class DashboardRepository implements DashboardRepositoryInterface
{
    public function getTotalItems(): int
    {
        return (int) Item::sum('total_quantity');
    }

    public function getAvailableItems(): int
    {
        return (int) Item::sum('available_quantity');
    }

    public function getCurrentlyBorrowedCount(): int
    {
        return (int) BorrowRecord::where('status', 'borrowed')
            ->join('borrow_record_items', 'borrow_records.id', '=', 'borrow_record_items.borrow_record_id')
            ->sum('borrow_record_items.quantity');
    }

    public function getDueTodayCount(): int
    {
        return BorrowRecord::where('status', 'borrowed')
            ->whereDate('due_at', Carbon::today())
            ->count();
    }

    public function getRecentTransactions(int $limit = 5)
    {
        return BorrowRecord::with(['student', 'items'])
            ->orderBy('created_at', 'desc')
            ->take($limit)
            ->get();
    }
}
