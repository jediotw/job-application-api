import { formatApiErrorMessage, toApiErrorInfo } from '../../lib/http'

export interface JobErrorInfo {
  message: string
  fieldErrors: Record<string, string>
  kind: ReturnType<typeof toApiErrorInfo>['kind']
}

export function toJobError(error: unknown, fallback: string): JobErrorInfo {
  const info = toApiErrorInfo(error, fallback)
  return {
    message: formatApiErrorMessage(error, fallback),
    fieldErrors: info.fieldErrors,
    kind: info.kind,
  }
}
