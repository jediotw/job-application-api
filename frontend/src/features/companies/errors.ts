import { HttpError } from '../../lib/http'
import { readStoredToken } from '../auth/session'

export const VALIDATION_FAILED_MESSAGE = 'Validation failed'
const DUPLICATE_RESOURCE_MESSAGE = 'Data conflict'
const DUPLICATE_COMPANY_MESSAGE = 'A company with this name or CIN already exists.'

export interface CompanyErrorInfo {
  message: string
  fieldErrors: Record<string, string>
}

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

function statusMessage(status: number): string | null {
  if (status === 404) return 'This company no longer exists.'
  if (status === 409) return DUPLICATE_COMPANY_MESSAGE
  if (status >= 500) return 'The server could not complete the request. Please try again.'

  return null
}

const SESSION_EXPIRED_MESSAGE = 'Your session has expired. Please sign in again.'
const NOT_ALLOWED_MESSAGE = 'You do not have permission to manage this company.'

export function toCompanyError(error: unknown, fallback: string): CompanyErrorInfo {
  if (error instanceof HttpError) {
    if (error.status === 401) {
      const message =
        readStoredToken() === null ? SESSION_EXPIRED_MESSAGE : NOT_ALLOWED_MESSAGE
      return { message, fieldErrors: {} }
    }

    if (error.status === 403) {
      const fieldErrors = bodyFieldErrors(error.body)
      return { message: bodyMessage(error.body) ?? NOT_ALLOWED_MESSAGE, fieldErrors }
    }

    const fieldErrors = bodyFieldErrors(error.body)
    let message = bodyMessage(error.body) ?? statusMessage(error.status) ?? fallback

    if (error.status === 409 && message === DUPLICATE_RESOURCE_MESSAGE) {
      message = DUPLICATE_COMPANY_MESSAGE
    }

    return { message, fieldErrors }
  }

  if (error instanceof TypeError) {
    return {
      message: 'Unable to reach the server. Check your connection and try again.',
      fieldErrors: {},
    }
  }

  return { message: fallback, fieldErrors: {} }
}

export function toCompanyErrorMessage(error: unknown, fallback: string): string {
  return toCompanyError(error, fallback).message
}
