<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Http\Requests\Users\StoreUserRequest;
use App\Http\Requests\Users\UpdateUserRequest;
use App\Http\Resources\UserResource;
use App\Services\UserService;
use Illuminate\Http\JsonResponse;
use Illuminate\Validation\ValidationException;

class UserController extends Controller
{
    protected $userService;

    public function __construct(UserService $userService)
    {
        $this->userService = $userService;
    }

    public function index(): JsonResponse
    {
        $users = $this->userService->listUsers();
        
        return $this->successResponse(
            UserResource::collection($users),
            'Users retrieved successfully.'
        );
    }

    public function store(StoreUserRequest $request): JsonResponse
    {
        $user = $this->userService->createUser($request->validated());

        return $this->successResponse(
            new UserResource($user),
            'User created successfully.',
            201
        );
    }

    public function update(UpdateUserRequest $request, int $id): JsonResponse
    {
        $user = $this->userService->updateUser($id, $request->validated());

        return $this->successResponse(
            new UserResource($user),
            'User updated successfully.'
        );
    }

    public function destroy(int $id): JsonResponse
    {
        try {
            $this->userService->deleteUser($id);
            return $this->successResponse(null, 'User deleted successfully.');
        } catch (ValidationException $e) {
            return $this->errorResponse($e->getMessage(), 422, $e->errors());
        }
    }

    public function resetPassword(int $id): JsonResponse
    {
        $user = $this->userService->resetPassword($id);

        return $this->successResponse(
            new UserResource($user),
            'User password has been reset to system default.'
        );
    }
}
