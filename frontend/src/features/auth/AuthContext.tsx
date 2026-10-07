import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { login as loginRequest, register as registerRequest } from './api'
import { apiClient } from '../../lib/http'
import { clearStoredToken, readStoredToken, readStoredUser, storeToken } from './session'
import type { AuthUser, LoginRequest, LoginResponse, RegisterRequest, UserResponse } from './types'

export interface AuthContextValue {
  user: AuthUser | null
  isAuthenticated: boolean
  login: (request: LoginRequest) => Promise<LoginResponse>
  register: (request: RegisterRequest) => Promise<UserResponse>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [token, setToken] = useState<string | null>(() => readStoredToken())
  const [user, setUser] = useState<AuthUser | null>(() => readStoredUser())

  const login = useCallback(async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await loginRequest(request)

    storeToken(response.token)
    setToken(response.token)
    setUser({ id: response.id, email: response.email, role: response.role })

    return response
  }, [])

  const register = useCallback((request: RegisterRequest): Promise<UserResponse> => {
    return registerRequest(request)
  }, [])

  const logout = useCallback(() => {
    clearStoredToken()
    setToken(null)
    setUser(null)
  }, [])

  useEffect(() => {
    apiClient.setUnauthorizedHandler(logout)
    return () => apiClient.setUnauthorizedHandler(null)
  }, [logout])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: token !== null && user !== null,
      login,
      register,
      logout,
    }),
    [token, user, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
