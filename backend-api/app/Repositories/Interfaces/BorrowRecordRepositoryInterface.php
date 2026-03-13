<?php

namespace App\Repositories\Interfaces;

interface BorrowRecordRepositoryInterface
{
    /**
     * Get active borrow records based on filters.
     *
     * @param array $filters
     * @return \Illuminate\Contracts\Pagination\LengthAwarePaginator
     */
    public function getActiveRecords(array $filters = []);

    /**
     * Find a record by ID.
     *
     * @param int $id
     * @return \App\Models\BorrowRecord
     */
    public function findById(int $id);

    /**
     * Update the status and return date of a record.
     *
     * @param \App\Models\BorrowRecord $record
     * @param string $status
     * @param \Illuminate\Support\Carbon|null $returnedAt
     * @return \App\Models\BorrowRecord
     */
    public function updateStatus(\App\Models\BorrowRecord $record, string $status, $returnedAt = null);
}
