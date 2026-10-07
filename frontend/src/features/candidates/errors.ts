import { formatApiErrorMessage, toApiErrorInfo } from '../../lib/http'

export const VALIDATION_FAILED_MESSAGE = 'Validation failed'

export interface CandidateErrorInfo {
  message: string
  fieldErrors: Record<string, string>
}

export function toCandidateError(error: unknown, fallback: string): CandidateErrorInfo {
  const info = toApiErrorInfo(error, fallback)
  return { message: formatApiErrorMessage(error, fallback), fieldErrors: info.fieldErrors }
}

export function toCandidateErrorMessage(error: unknown, fallback: string): string {
  return formatApiErrorMessage(error, fallback)
}
