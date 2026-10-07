import { HttpError } from './errors'

export type ApiErrorKind =
  | 'validation'
  | 'unauthorized'
  | 'forbidden'
  | 'not-found'
  | 'conflict'
  | 'server'
  | 'network'
  | 'unknown'

export interface ApiErrorInfo {
  kind: ApiErrorKind
  message: string
  fieldErrors: Record<string, string>
  requestId?: string
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

  return Object.fromEntries(
    Object.entries(errors as Record<string, unknown>).filter(
      ([, message]) => typeof message === 'string' && message.trim(),
    ),
  ) as Record<string, string>
}

export function toApiErrorInfo(error: unknown, fallback = 'Something went wrong.'): ApiErrorInfo {
  if (error instanceof HttpError) {
    const fieldErrors = bodyFieldErrors(error.body)
    const responseMessage = bodyMessage(error.body)

    if (error.status === 400 || error.status === 422) {
      return { kind: 'validation', message: responseMessage ?? 'Please correct the highlighted fields.', fieldErrors }
    }
    if (error.status === 401) {
      return { kind: 'unauthorized', message: 'Your session has expired. Please sign in again.', fieldErrors }
    }
    if (error.status === 403) {
      return { kind: 'forbidden', message: responseMessage ?? 'You do not have permission to perform this action.', fieldErrors }
    }
    if (error.status === 404) {
      return { kind: 'not-found', message: responseMessage ?? 'The requested resource was not found.', fieldErrors }
    }
    if (error.status === 409) {
      return { kind: 'conflict', message: responseMessage ?? 'This request conflicts with existing data.', fieldErrors }
    }
    if (error.status >= 500) {
      return { kind: 'server', message: 'Something went wrong.', fieldErrors, requestId: error.requestId }
    }

    return { kind: 'unknown', message: responseMessage ?? fallback, fieldErrors, requestId: error.requestId }
  }

  if (error instanceof TypeError) {
    return { kind: 'network', message: 'The backend is unavailable. Check your connection and try again.', fieldErrors: {} }
  }

  return { kind: 'unknown', message: fallback, fieldErrors: {} }
}

export function formatApiErrorMessage(error: unknown, fallback?: string): string {
  const info = toApiErrorInfo(error, fallback)
  if (info.kind === 'server' && info.requestId) {
    return `Something went wrong.\\n\\nRequest ID: ${info.requestId}\\nPlease provide this ID when reporting the problem.`
  }
  return info.message
}
