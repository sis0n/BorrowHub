<?php

namespace App\Services;

use App\Repositories\Interfaces\UserRepositoryInterface;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class UserService
{
    protected $userRepository;
    protected $logService;

    public function __construct(UserRepositoryInterface $userRepository, LogService $logService)
    {
        $this->userRepository = $userRepository;
        $this->logService = $logService;
    }

    public function listUsers()
    {
        return $this->userRepository->getAll();
    }

    public function getUser(int $id)
    {
        return $this->userRepository->findById($id);
    }

    public function createUser(array $data)
    {
        $data['password'] = Hash::make($data['password']);
        $user = $this->userRepository->create($data);

        $this->logService->log(
            'User Created',
            "Created user with username: {$user->username}, Role: {$user->role}",
            (string)$user->id,
            $user->name
        );

        return $user;
    }

    public function updateUser(int $id, array $data)
    {
        if (isset($data['password'])) {
            $data['password'] = Hash::make($data['password']);
        }
        
        $user = $this->userRepository->update($id, $data);

        $this->logService->log(
            'User Updated',
            "Updated user fields: " . implode(', ', array_keys($data)),
            (string)$user->id,
            $user->name
        );

        return $user;
    }

    public function deleteUser(int $id)
    {
        $user = $this->userRepository->findById($id);

        if ($user->username === 'admin') {
            throw ValidationException::withMessages([
                'user' => 'The primary administrator account cannot be deleted.'
            ]);
        }

        $result = $this->userRepository->delete($id);

        if ($result) {
            $this->logService->log(
                'User Deleted',
                "Deleted user with username: {$user->username}",
                (string)$user->id,
                $user->name
            );
        }

        return $result;
    }

    public function resetPassword(int $id)
    {
        $defaultPassword = config('auth.default_user_password');
        
        if (!$defaultPassword) {
            throw new \RuntimeException('Default user password is not configured.');
        }

        $user = $this->userRepository->update($id, [
            'password' => Hash::make($defaultPassword)
        ]);

        $this->logService->log(
            'Password Reset',
            "Reset password for user",
            (string)$user->id,
            $user->name
        );

        return $user;
    }
}
