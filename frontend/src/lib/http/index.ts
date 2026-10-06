export { HttpClient, API_BASE_URL } from './client'
export type { RequestOptions, QueryParams, ResponseInfo } from './client'
export { HttpError } from './errors'
export { generateRequestId } from './requestId'

import { HttpClient } from './client'

export const apiClient = new HttpClient()
