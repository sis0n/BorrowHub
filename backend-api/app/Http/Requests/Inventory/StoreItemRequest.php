<?php

namespace App\Http\Requests\Inventory;

use Illuminate\Foundation\Http\FormRequest;

class StoreItemRequest extends FormRequest
{
    public function authorize(): bool
    {
        return $this->user() && $this->user()->role === 'admin';
    }

    public function rules(): array
    {
        return [
            'category_id' => 'required|exists:categories,id',
            'name' => 'required|string|max:255',
            'total_quantity' => 'required|integer|min:0',
            'available_quantity' => 'nullable|integer|min:0|lte:total_quantity',
            'status' => 'required|in:active,maintenance,archived',
        ];
    }
}