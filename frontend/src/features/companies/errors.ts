import { formatApiErrorMessage, toApiErrorInfo } from '../../lib/http'

export const VALIDATION_FAILED_MESSAGE = 'Validation failed'

export interface CompanyErrorInfo {
  message: string
  fieldErrors: Record<string, string>
}

export function toCompanyError(error: unknown, fallback: string): CompanyErrorInfo {
  const info = toApiErrorInfo(error, fallback)
  return { message: formatApiErrorMessage(error, fallback), fieldErrors: info.fieldErrors }
}

export function toCompanyErrorMessage(error: unknown, fallback: string): string {
  return formatApiErrorMessage(error, fallback)
}
