<?php

namespace App\Http\Requests\Inventory;

use Illuminate\Foundation\Http\FormRequest;
use App\Rules\ValidAvailableQuantity;

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
                new ValidAvailableQuantity($this->route('item'), $this->input('total_quantity'))
            ],
            'status' => 'sometimes|in:active,maintenance,archived',
        ];
    }
}
