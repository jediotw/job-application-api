import { apiClient } from '../../../lib/http'
import type { LoginRequest, LoginResponse, RegisterRequest, UserResponse } from '../types'

export function register(request: RegisterRequest): Promise<UserResponse> {
  return apiClient.post<UserResponse>('/auth/register', request)
}

export function login(request: LoginRequest): Promise<LoginResponse> {
  return apiClient.post<LoginResponse>('/auth/login', request)
}
