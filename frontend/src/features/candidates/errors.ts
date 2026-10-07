import { HttpError } from '../../lib/http'

export const VALIDATION_FAILED_MESSAGE = 'Validation failed'
const DUPLICATE_RESOURCE_MESSAGE = 'Data conflict'
const DUPLICATE_EMAIL_MESSAGE = 'This email is already used by another profile.'

export interface CandidateErrorInfo {
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
  if (status === 404) return 'This profile no longer exists.'
  if (status === 409) return 'This change conflicts with data that already exists.'
  if (status >= 500) return 'The server could not complete the request. Please try again.'

  return null
}

export function toCandidateError(error: unknown, fallback: string): CandidateErrorInfo {
  if (error instanceof HttpError) {
    if (error.status === 401) {
      return { message: 'Your session has expired. Please sign in again.', fieldErrors: {} }
    }

    const fieldErrors = bodyFieldErrors(error.body)
    let message = bodyMessage(error.body) ?? statusMessage(error.status) ?? fallback

    if (error.status === 409 && message === DUPLICATE_RESOURCE_MESSAGE) {
      message = DUPLICATE_EMAIL_MESSAGE
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

export function toCandidateErrorMessage(error: unknown, fallback: string): string {
  return toCandidateError(error, fallback).message
}
