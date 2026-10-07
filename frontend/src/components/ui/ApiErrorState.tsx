import Alert from './Alert'
import Button from './Button'
import type { ApiErrorInfo } from '../../lib/http'

interface ApiErrorStateProps {
  error: ApiErrorInfo
  onRetry?: () => void
}

function ApiErrorState({ error, onRetry }: ApiErrorStateProps) {
  const title =
    error.kind === 'forbidden' ? 'Access forbidden' :
    error.kind === 'not-found' ? 'Not found' :
    error.kind === 'conflict' ? 'Conflict' :
    error.kind === 'network' ? 'Backend unavailable' :
    error.kind === 'server' ? 'Server error' :
    error.kind === 'unauthorized' ? 'Authentication required' :
    'Request failed'

  return (
    <div className="api-error-state">
      <Alert variant="error" title={title}>
        {error.message}
        {error.kind === 'server' && error.requestId ? (
          <span className="api-error-request-id">
            Request ID: {error.requestId}. Please provide this ID when reporting the problem.
          </span>
        ) : null}
      </Alert>
      {onRetry ? <Button variant="secondary" onClick={onRetry}>Try again</Button> : null}
    </div>
  )
}

export default ApiErrorState
