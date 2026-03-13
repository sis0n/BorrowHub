<?php

namespace App\Repositories\Eloquent;

use App\Models\BorrowRecord;
use App\Repositories\Interfaces\BorrowRecordRepositoryInterface;

class EloquentBorrowRecordRepository implements BorrowRecordRepositoryInterface
{
    public function getActiveRecords(array $filters = [])
    {
        $query = BorrowRecord::with(['student', 'items', 'staff'])
            ->where('status', 'borrowed');

        if (isset($filters['student_number'])) {
            $query->whereHas('student', function ($q) use ($filters) {
                $q->where('student_number', 'like', '%' . $filters['student_number'] . '%');
            });
        }

        if (isset($filters['item_name'])) {
            $query->whereHas('items', function ($q) use ($filters) {
                $q->where('name', 'like', '%' . $filters['item_name'] . '%');
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
}
