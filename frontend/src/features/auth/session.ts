import type { AuthUser, Role } from './types'

const TOKEN_STORAGE_KEY = 'job-application.auth.token'

interface TokenPayload {
  sub?: string
  userId?: number
  role?: string
  exp?: number
}

function decodePayload(token: string): TokenPayload | null {
  const segment = token.split('.')[1]
  if (!segment) return null

  try {
    const base64 = segment.replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(base64)) as TokenPayload
  } catch {
    return null
  }
}

function isExpired(payload: TokenPayload): boolean {
  if (typeof payload.exp !== 'number') return true
  return payload.exp * 1000 <= Date.now()
}

function normalizeRole(role: string | undefined): Role {
  return role === 'RECRUITER' ? 'RECRUITER' : 'CANDIDATE'
}

function readToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_STORAGE_KEY)
  } catch {
    return null
  }
}

export function readStoredToken(): string | null {
  const token = readToken()
  if (!token) return null

  const payload = decodePayload(token)
  if (!payload || isExpired(payload)) {
    clearStoredToken()
    return null
  }

  return token
}

export function storeToken(token: string): void {
  try {
    localStorage.setItem(TOKEN_STORAGE_KEY, token)
  } catch {
    // Storage unavailable: session stays in memory only.
  }
}

export function clearStoredToken(): void {
  try {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
  } catch {
    // Storage unavailable: nothing to clear.
  }
}

export function readStoredUser(): AuthUser | null {
  const token = readStoredToken()
  if (!token) return null

  const payload = decodePayload(token)
  if (!payload || typeof payload.userId !== 'number' || typeof payload.sub !== 'string') {
    return null
  }

  return {
    id: payload.userId,
    email: payload.sub,
    role: normalizeRole(payload.role),
  }
}

export function isAuthenticated(): boolean {
  return readStoredToken() !== null
}
