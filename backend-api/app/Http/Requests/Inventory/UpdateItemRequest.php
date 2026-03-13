<?php

namespace App\Http\Requests\Inventory;

use Illuminate\Foundation\Http\FormRequest;

class UpdateItemRequest extends FormRequest
{
    public function authorize(): bool
    {
        return $this->user() && $this->user()->role === 'admin';
    }

    public function rules(): array
    {
        return [
            'category_id' => 'sometimes|exists:categories,id',
            'name' => 'sometimes|string|max:255',
            'total_quantity' => 'sometimes|integer|min:0',
            'available_quantity' => [
                'sometimes',
                'integer',
                'min:0',
                function ($attribute, $value, $fail) {
                    $item = \App\Models\Item::find($this->route('item'));
                    if (!$item) return;

                    $totalQuantity = $this->input('total_quantity', $item->total_quantity);

                    if ($value > $totalQuantity) {
                        $fail('The available quantity cannot be greater than the total quantity.');
                    }
                },
            ],
            'status' => 'sometimes|in:active,maintenance,archived',
        ];
    }
}
