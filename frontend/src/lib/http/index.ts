export { HttpClient, API_BASE_URL } from './client'
export type { RequestOptions, QueryParams } from './client'
export { HttpError } from './errors'

import { HttpClient } from './client'

export const apiClient = new HttpClient()
