export { HttpClient, API_BASE_URL } from './client'
export type { RequestOptions, QueryParams, ResponseInfo, AuthTokenProvider } from './client'
export { HttpError } from './errors'
export { generateRequestId } from './requestId'

import { HttpClient } from './client'

export const apiClient = new HttpClient()
\nexport { formatApiErrorMessage, toApiErrorInfo } from './errorHandling'\nexport type { ApiErrorInfo, ApiErrorKind } from './errorHandling'\n