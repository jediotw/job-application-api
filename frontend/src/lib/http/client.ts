import { HttpError } from './errors'
import { generateRequestId } from './requestId'

export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')

export type QueryParams = Record<string, string | number | boolean | undefined>

export type AuthTokenProvider = () => string | null | undefined

export interface ResponseInfo {
  status: number
  sentRequestId: string
  responseRequestId?: string
}

export interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown
  query?: QueryParams
  requestId?: string
  onResponse?: (info: ResponseInfo) => void
}

export class HttpClient {
  private readonly baseUrl: string
  private readonly fetchFn: typeof fetch
  private authTokenProvider: AuthTokenProvider | null = null
  private unauthorizedHandler: (() => void) | null = null

  constructor(baseUrl: string = API_BASE_URL, fetchFn: typeof fetch = globalThis.fetch.bind(globalThis)) {
    this.baseUrl = baseUrl.replace(/\/+$/, '')
    this.fetchFn = fetchFn
  }

  setAuthTokenProvider(provider: AuthTokenProvider | null): void {
    this.authTokenProvider = provider
  }

  setUnauthorizedHandler(handler: (() => void) | null): void {
    this.unauthorizedHandler = handler
  }

  get<T>(path: string, options: RequestOptions = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: 'GET' })
  }

  post<T>(path: string, body?: unknown, options: RequestOptions = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: 'POST', body })
  }

  put<T>(path: string, body?: unknown, options: RequestOptions = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: 'PUT', body })
  }

  patch<T>(path: string, body?: unknown, options: RequestOptions = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: 'PATCH', body })
  }

  delete<T>(path: string, options: RequestOptions = {}): Promise<T> {
    return this.request<T>(path, { ...options, method: 'DELETE' })
  }

  async request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const { body, query, headers, requestId, onResponse, ...init } = options
    const sentRequestId = requestId ?? generateRequestId()

    const response = await this.fetchFn(this.buildUrl(path, query), {
      ...init,
      headers: this.buildHeaders(body, headers, sentRequestId),
      ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
    })

    const responseRequestId = response.headers.get('X-Request-ID') ?? undefined
    onResponse?.({ status: response.status, sentRequestId, responseRequestId })

    const data = await this.parseBody(response)

    if (!response.ok) {
      if (response.status === 401) this.unauthorizedHandler?.()

      throw new HttpError(
        response.status,
        response.statusText,
        data,
        responseRequestId ?? sentRequestId,
      )
    }

    return data as T
  }

  private buildUrl(path: string, query?: QueryParams): string {
    const normalizedPath = path.replace(/^\/+/, '')
    const url = `${this.baseUrl}/${normalizedPath}`

    if (!query) return url

    const params = new URLSearchParams()
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined) params.set(key, String(value))
    }

    const search = params.toString()
    return search ? `${url}?${search}` : url
  }

  private buildHeaders(body: unknown, headers?: HeadersInit, requestId?: string): Headers {
    const result = new Headers(headers)

    if (!result.has('Accept')) result.set('Accept', 'application/json')
    if (requestId && !result.has('X-Request-ID')) {
      result.set('X-Request-ID', requestId)
    }
    if (body !== undefined && !result.has('Content-Type')) {
      result.set('Content-Type', 'application/json')
    }

    const token = this.authTokenProvider?.()
    if (token && !result.has('Authorization')) {
      result.set('Authorization', `Bearer ${token}`)
    }

    return result
  }

  private async parseBody(response: Response): Promise<unknown> {
    if (response.status === 204) return undefined

    const text = await response.text()
    if (!text) return undefined

    const contentType = response.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
      try {
        return JSON.parse(text)
      } catch {
        return text
      }
    }

    return text
  }
}
