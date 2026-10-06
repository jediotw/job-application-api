export type Role = 'CANDIDATE' | 'RECRUITER'

export interface RegisterRequest {
  email: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface UserResponse {
  id: number
  email: string
  role: Role
}

export interface LoginResponse {
  token: string
  id: number
  email: string
  role: Role
}
