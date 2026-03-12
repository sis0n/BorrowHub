<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Relations\Pivot;

class BorrowRecordItem extends Pivot
{
    public $timestamps = false;

    protected $fillable = [
        'borrow_record_id',
        'item_id',
        'quantity',
    ];

    public function borrowRecord()
    {
        return $this->belongsTo(BorrowRecord::class);
    }

    public function item()
    {
        return $this->belongsTo(Item::class);
    }
}
