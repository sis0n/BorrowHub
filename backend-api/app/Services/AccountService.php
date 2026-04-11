<?php

namespace App\Services;

use App\Models\User;
use App\Repositories\Interfaces\UserRepositoryInterface;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class AccountService
{
    protected $userRepository;
    protected $logService;

    public function __construct(UserRepositoryInterface $userRepository, LogService $logService)
    {
        $this->userRepository = $userRepository;
        $this->logService = $logService;
    }

    /**
     * Update the user's profile information.
     *
     * @param User $user
     * @param array $data
     * @return User
     */
    public function updateProfile(User $user, array $data)
    {
        $updatedUser = $this->userRepository->update($user->id, [
            'name' => $data['name'],
            'username' => $data['username'],
        ]);

        $this->logService->log(
            LogService::ACTION_UPDATED,
            "Updated account profile (name/username).",
            (string)$updatedUser->id,
            'user'
        );

        return $updatedUser;
    }

    /**
     * Update the user's password.
     *
     * @param User $user
     * @param array $data
     * @return User
     * @throws ValidationException
     */
    public function updatePassword(User $user, array $data)
    {
        if (!Hash::check($data['current_password'], $user->password)) {
            throw ValidationException::withMessages([
                'current_password' => ['The provided password does not match your current password.'],
            ]);
        }

        $updatedUser = $this->userRepository->update($user->id, [
            'password' => Hash::make($data['new_password'])
        ]);

        $this->logService->log(
            LogService::ACTION_UPDATED,
            "Updated account password.",
            (string)$updatedUser->id,
            'user'
        );

        return $updatedUser;
    }
}
