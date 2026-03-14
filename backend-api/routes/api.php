<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\V1\CategoryController;
use App\Http\Controllers\Api\V1\ItemController;
use App\Http\Controllers\Api\V1\DashboardController;
use App\Http\Controllers\Api\V1\StudentController;
use App\Http\Controllers\Api\V1\TransactionController;

Route::prefix('v1')->group(function () {
    // Public routes
    Route::post('/login', [AuthController::class, 'login'])->middleware('throttle:5,1');

    // Protected routes
    Route::middleware('auth:sanctum')->group(function () {
        Route::post('/logout', [AuthController::class, 'logout']);
        Route::get('/user', [AuthController::class, 'user']);

        Route::get('/dashboard', [DashboardController::class, 'index']);

        Route::get('/categories', [CategoryController::class, 'index']);
        
        Route::apiResource('items', ItemController::class);

        // Student Management Routes
        Route::post('/students/import', [StudentController::class, 'import']);
        Route::apiResource('students', StudentController::class);
        
        // Transaction Routes
        Route::get('/transactions/active', [TransactionController::class, 'index']);
        Route::post('/transactions/borrow', [TransactionController::class, 'borrow']);
        Route::post('/transactions/{id}/return', [TransactionController::class, 'returnItem']);
    });
});
