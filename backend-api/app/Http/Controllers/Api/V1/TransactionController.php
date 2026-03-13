<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Http\Requests\Transactions\BorrowRequest;
use App\Http\Resources\BorrowRecordResource;
use App\Services\TransactionService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\ValidationException;

class TransactionController extends Controller
{
    protected $transactionService;

    public function __construct(TransactionService $transactionService)
    {
        $this->transactionService = $transactionService;
    }

    /**
     * Get active borrow transactions.
     */
    public function index(Request $request): JsonResponse
    {
        $filters = $request->only(['student_number', 'item_name']);
        $records = $this->transactionService->searchActiveTransactions($filters);

        return $this->successResponse(
            BorrowRecordResource::collection($records),
            'Active transactions retrieved successfully.'
        );
    }

    /**
     * Process a borrow transaction.
     */
    public function borrow(BorrowRequest $request): JsonResponse
    {
        try {
            $record = $this->transactionService->processBorrow($request->validated());

            return $this->successResponse(
                new BorrowRecordResource($record),
                'Borrow transaction processed successfully.',
                201
            );
        } catch (ValidationException $e) {
            return $this->errorResponse($e->getMessage(), 422, $e->errors());
        } catch (\Exception $e) {
            return $this->errorResponse('An error occurred while processing the transaction.', 500);
        }
    }

    /**
     * Process a return transaction.
     */
    public function returnItem(int $id): JsonResponse
    {
        try {
            $record = $this->transactionService->processReturn($id);

            return $this->successResponse(
                new BorrowRecordResource($record),
                'Item returned successfully.'
            );
        } catch (ValidationException $e) {
            return $this->errorResponse($e->getMessage(), 422, $e->errors());
        } catch (\Exception $e) {
            return $this->errorResponse('An error occurred while processing the return.', 500);
        }
    }
}
