<?php

namespace App\Providers;

use Illuminate\Support\ServiceProvider;
use App\Repositories\Interfaces\UserRepositoryInterface;
use App\Repositories\Eloquent\UserRepository;
use App\Repositories\Interfaces\CategoryRepositoryInterface;
use App\Repositories\Eloquent\CategoryRepository;
use App\Repositories\Interfaces\StudentRepositoryInterface;
use App\Repositories\Eloquent\EloquentStudentRepository;
use App\Repositories\Interfaces\ItemRepositoryInterface;
use App\Repositories\Eloquent\ItemRepository;
use App\Repositories\Interfaces\BorrowRecordRepositoryInterface;
use App\Repositories\Eloquent\EloquentBorrowRecordRepository;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        $this->app->bind(UserRepositoryInterface::class, UserRepository::class);

        $this->app->bind(
            StudentRepositoryInterface::class,
            EloquentStudentRepository::class
        );

        // Inventory Bindings
        $this->app->bind(
            CategoryRepositoryInterface::class,
            CategoryRepository::class
        );
        $this->app->bind(ItemRepositoryInterface::class, ItemRepository::class);

        // Transaction Bindings
        $this->app->bind(
            BorrowRecordRepositoryInterface::class,
            EloquentBorrowRecordRepository::class
        );
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        //
    }
}
