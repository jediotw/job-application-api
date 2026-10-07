export class HttpError extends Error {
  readonly status: number
  readonly statusText: string
  readonly body: unknown
  readonly requestId?: string

  constructor(status: number, statusText: string, body: unknown, requestId?: string) {
    super(`HTTP ${status} ${statusText}`)
    this.name = 'HttpError'
    this.status = status
    this.statusText = statusText
    this.body = body
    this.requestId = requestId
  }
}
