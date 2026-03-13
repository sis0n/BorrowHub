<?php

namespace App\Services;

use App\Models\BorrowRecord;
use App\Repositories\Interfaces\BorrowRecordRepositoryInterface;
use App\Repositories\Interfaces\ItemRepositoryInterface;
use App\Repositories\Interfaces\StudentRepositoryInterface;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class TransactionService
{
    protected $itemRepository;
    protected $studentRepository;
    protected $borrowRecordRepository;

    public function __construct(
        ItemRepositoryInterface $itemRepository,
        StudentRepositoryInterface $studentRepository,
        BorrowRecordRepositoryInterface $borrowRecordRepository
    ) {
        $this->itemRepository = $itemRepository;
        $this->studentRepository = $studentRepository;
        $this->borrowRecordRepository = $borrowRecordRepository;
    }

    public function searchActiveTransactions(array $filters)
    {
        return $this->borrowRecordRepository->getActiveRecords($filters);
    }

    public function processBorrow(array $data)
    {
        return DB::transaction(function () use ($data) {
            $student = $this->studentRepository->findByStudentNumber($data['student_number']);
            if (!$student) {
                throw ValidationException::withMessages(['student_number' => 'Student not found.']);
            }

            $dueAt = Carbon::now()->setTime(config('borrow.due_hour', 20), 0, 0);

            $borrowRecord = BorrowRecord::create([
                'student_id' => $student->id,
                'staff_id' => Auth::id(),
                'collateral' => $data['collateral'] ?? null,
                'borrowed_at' => Carbon::now(),
                'due_at' => $dueAt,
                'status' => 'borrowed',
            ]);

            foreach ($data['items'] as $itemData) {
                $item = $this->itemRepository->findById($itemData['id'], true);

                if ($item->available_quantity < $itemData['quantity']) {
                    throw ValidationException::withMessages([
                        'items' => "Insufficient stock for items: {$item->name}"
                    ]);
                }

                $borrowRecord->items()->attach($item->id, [
                    'quantity' => $itemData['quantity']
                ]);

                $this->itemRepository->decrementAvailableQuantity($item, $itemData['quantity']);
            }

            return $borrowRecord->load(['student', 'items', 'staff']);
        });
    }

    public function processReturn(int $recordId)
    {
        return DB::transaction(function () use ($recordId) {
            $record = $this->borrowRecordRepository->findById($recordId);

            if ($record->status !== 'borrowed') {
                throw ValidationException::withMessages([
                    'id' => 'This transaction is already processed or invalid.'
                ]);
            }

            $this->borrowRecordRepository->updateStatus($record, 'returned', Carbon::now());

            foreach ($record->items as $item) {
                $this->itemRepository->incrementAvailableQuantity($item, $item->pivot->quantity);
            }

            $record->refresh();

            return $record->load(['student', 'items', 'staff']);
        });
    }
}
