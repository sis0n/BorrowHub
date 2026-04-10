<?php

namespace App\Repositories\Eloquent;

use App\Models\BorrowRecord;
use App\Repositories\Interfaces\BorrowRecordRepositoryInterface;

class EloquentBorrowRecordRepository implements BorrowRecordRepositoryInterface
{
    public function getActiveRecords(array $filters = [])
    {
        $query = BorrowRecord::with(['student.course', 'items', 'staff'])
            ->where('status', 'borrowed');
// Filter by Student Number
if (isset($filters['student_number'])) {
    $query->whereHas('student', function ($q) use ($filters) {
        $q->where('student_number', 'like', $filters['student_number'] . '%');
    });
}

// Filter by Item Name
if (isset($filters['item_name'])) {
    $query->whereHas('items', function ($q) use ($filters) {
        $q->where('name', 'like', $filters['item_name'] . '%');
    });
}

        return $query->latest()->paginate(config('borrow.pagination_size', 15));
    }

    public function findById(int $id)
    {
        return BorrowRecord::with(['items'])->findOrFail($id);
    }

    public function updateStatus(\App\Models\BorrowRecord $record, string $status, $returnedAt = null)
    {
        $record->update([
            'status' => $status,
            'returned_at' => $returnedAt
        ]);
        return $record;
    }

    public function getTransactionHistory(array $filters = [])
    {
        $query = BorrowRecord::with(['student.course', 'items', 'staff']);

        // Filter by search (student name or student number)
        if (!empty($filters['search'])) {
            $search = $filters['search'];
            $query->whereHas('student', function ($q) use ($search) {
                $q->where('name', 'like', '%' . $search . '%')
                  ->orWhere('student_number', 'like', '%' . $search . '%');
            });
        }

        // Filter by status
        if (!empty($filters['status'])) {
            $query->where('status', $filters['status']);
        }

        // Filter by date range
        if (!empty($filters['date_from'])) {
            $query->whereDate('borrowed_at', '>=', $filters['date_from']);
        }

        if (!empty($filters['date_to'])) {
            $query->whereDate('borrowed_at', '<=', $filters['date_to']);
        }

        return $query->latest()->paginate(config('borrow.pagination_size', 15));
    }
}
