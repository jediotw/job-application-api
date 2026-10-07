import { HttpError } from '../../lib/http'

function bodyMessage(body: unknown): string | null {
  if (typeof body === 'string' && body.trim()) return body.trim()

  if (body && typeof body === 'object' && 'message' in body) {
    const message = (body as { message?: unknown }).message
    if (typeof message === 'string' && message.trim()) return message.trim()
  }

  return null
}

export function toAuthErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpError) {
    const message = bodyMessage(error.body)
    if (message) return message

    if (error.status === 401) return 'Invalid email or password.'
    if (error.status === 409) return 'An account with this email already exists.'
  }

  return fallback
}
