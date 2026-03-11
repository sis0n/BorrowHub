<?php

namespace App\Services;

use App\Repositories\Interfaces\UserRepositoryInterface;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class AuthService
{
    protected $userRepository;

    public function __construct(UserRepositoryInterface $userRepository)
    {
        $this->userRepository = $userRepository;
    }

    /**
     * Authenticate user and return token.
     *
     * @param array $credentials
     * @return array
     * @throws ValidationException
     */
    public function authenticate(array $credentials)
    {
        $user = $this->userRepository->findByUsername($credentials['username']);

        if (!$user || !Hash::check($credentials['password'], $user->password)) {
            throw ValidationException::withMessages([
                'username' => ['The provided credentials do not match our records.'],
            ]);
        }

        // Revoke all existing tokens to enforce single session (optional, but good practice for mobile)
        $user->tokens()->delete();

        $token = $user->createToken('mobile-app-token')->plainTextToken;

        return [
            'user' => $user,
            'token' => $token,
        ];
    }
}
