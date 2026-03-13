<?php

namespace App\Http\Requests\Inventory;

use Illuminate\Foundation\Http\FormRequest;

class UpdateItemRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'category_id' => 'nullable|exists:categories,id',
            'name' => 'nullable|string|max:255',
            'total_quantity' => 'nullable|integer|min:0',
            'available_quantity' => 'nullable|integer|min:0|lte:total_quantity',
            'status' => 'nullable|in:active,maintenance,archived',
        ];
    }
}