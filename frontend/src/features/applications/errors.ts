import { HttpError } from '../../lib/http'
import { readStoredToken } from '../auth/session'

const SESSION_EXPIRED_MESSAGE = 'Your session has expired. Please sign in again.'
const NOT_ALLOWED_MESSAGE = 'You do not have permission to access this application.'
const NOT_FOUND_MESSAGE = 'This application no longer exists.'
const VALIDATION_MESSAGE = 'Please correct the highlighted fields.'
const SERVER_MESSAGE = 'The server could not complete the request. Please try again.'

function bodyMessage(body: unknown): string | null {
  if (typeof body === 'string' && body.trim()) return body.trim()
  if (body && typeof body === 'object' && 'message' in body) {
    const message = (body as { message?: unknown }).message
    if (typeof message === 'string' && message.trim()) return message.trim()
  }
  return null
}

function bodyFieldErrors(body: unknown): Record<string, string> {
  if (!body || typeof body !== 'object' || !('errors' in body)) return {}
  const errors = (body as { errors?: unknown }).errors
  if (!errors || typeof errors !== 'object') return {}
  const fieldErrors: Record<string, string> = {}
  for (const [field, message] of Object.entries(errors as Record<string, unknown>)) {
    if (typeof message === 'string' && message.trim()) fieldErrors[field] = message.trim()
  }
  return fieldErrors
}

export interface ApplicationErrorInfo {
  message: string
  fieldErrors: Record<string, string>
}

export function toApplicationError(error: unknown, fallback: string): ApplicationErrorInfo {
  if (error instanceof HttpError) {
    if (error.status === 401) {
      return {
        message: readStoredToken() === null ? SESSION_EXPIRED_MESSAGE : 'Authentication is required.',
        fieldErrors: {},
      }
    }
    if (error.status === 403) {
      return { message: bodyMessage(error.body) ?? NOT_ALLOWED_MESSAGE, fieldErrors: bodyFieldErrors(error.body) }
    }
    if (error.status === 404) {
      return { message: bodyMessage(error.body) ?? NOT_FOUND_MESSAGE, fieldErrors: bodyFieldErrors(error.body) }
    }
    if (error.status === 400 || error.status === 422) {
      return { message: bodyMessage(error.body) ?? VALIDATION_MESSAGE, fieldErrors: bodyFieldErrors(error.body) }
    }
    if (error.status >= 500) {
      return { message: bodyMessage(error.body) ?? SERVER_MESSAGE, fieldErrors: bodyFieldErrors(error.body) }
    }
    return { message: bodyMessage(error.body) ?? fallback, fieldErrors: bodyFieldErrors(error.body) }
  }

  if (error instanceof TypeError) {
    return { message: 'Unable to reach the server. Check your connection and try again.', fieldErrors: {} }
  }

  return { message: fallback, fieldErrors: {} }
}

export function toApplicationErrorMessage(error: unknown, fallback: string): string {
  return toApplicationError(error, fallback).message
}
