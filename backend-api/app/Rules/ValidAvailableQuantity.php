<?php

namespace App\Rules;

use App\Repositories\Interfaces\ItemRepositoryInterface;
use Closure;
use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Support\Facades\App;

class ValidAvailableQuantity implements ValidationRule
{
    protected $itemId;
    protected $totalQuantityInput;

    public function __construct($itemId, $totalQuantityInput = null)
    {
        $this->itemId = $itemId;
        $this->totalQuantityInput = $totalQuantityInput;
    }

    public function validate(string $attribute, mixed $value, Closure $fail): void
    {
        $itemRepository = App::make(ItemRepositoryInterface::class);
        $item = $itemRepository->findById($this->itemId);

        if (!$item) {
            return;
        }
        $totalQuantity = $this->totalQuantityInput ?? $item->total_quantity;

        if ($value > $totalQuantity) {
            $fail('The available quantity cannot be greater than the total quantity.');
        }
    }
}