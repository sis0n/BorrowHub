<?php

namespace App\Services;

use App\Repositories\Interfaces\UserRepositoryInterface;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class UserService
{
    protected $userRepository;

    public function __construct(UserRepositoryInterface $userRepository)
    {
        $this->userRepository = $userRepository;
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
        return $this->userRepository->create($data);
    }

    public function updateUser(int $id, array $data)
    {
        if (isset($data['password'])) {
            $data['password'] = Hash::make($data['password']);
        }
        return $this->userRepository->update($id, $data);
    }

    public function deleteUser(int $id)
    {
        $user = $this->userRepository->findById($id);

        if ($user->username === 'admin') {
            throw ValidationException::withMessages([
                'user' => 'The primary administrator account cannot be deleted.'
            ]);
        }

        return $this->userRepository->delete($id);
    }

    public function resetPassword(int $id)
    {
        $defaultPassword = config('auth.default_user_password', 'borrowhub123');
        
        return $this->userRepository->update($id, [
            'password' => Hash::make($defaultPassword)
        ]);
    }
}
