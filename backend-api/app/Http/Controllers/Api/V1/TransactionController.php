<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Http\Requests\Transactions\BorrowRequest;
use App\Http\Resources\BorrowRecordResource;
use App\Services\TransactionService;
use Illuminate\Http\JsonResponse;
use Illuminate\Validation\ValidationException;

class TransactionController extends Controller
{
    protected $transactionService;

    public function __construct(TransactionService $transactionService)
    {
        $this->transactionService = $transactionService;
    }

    public function borrow(BorrowRequest $request): JsonResponse
    {
        try {
            $record =
                $this->transactionService->processBorrow($request->validated());

            return $this->successResponse(
                new BorrowRecordResource($record),
                'Borrow transaction processed successfully.',
                201
            );
        } catch (ValidationException $e) {
            return $this->errorResponse(
                $e->getMessage(),
                422,
                $e->errors()
            );
        } catch (\Exception $e) {
            return $this->errorResponse('An error occurred while processing the transaction.', 500);
        }
    }
}
