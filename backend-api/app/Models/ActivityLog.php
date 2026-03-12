<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class ActivityLog extends Model
{
    use HasFactory;

    const UPDATED_AT = null;

    protected $fillable = [
        'actor_id',
        'performed_by',
        'target_user_id',
        'target_user_name',
        'action',
        'details',
        'type',
    ];

    public function actor()
    {
        return $this->belongsTo(User::class, 'actor_id');
    }
}
