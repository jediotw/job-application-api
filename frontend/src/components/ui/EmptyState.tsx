import type { ReactNode } from 'react'

interface EmptyStateProps {
  title: ReactNode
  message?: ReactNode
  children?: ReactNode
}

function EmptyState({ title, message, children }: EmptyStateProps) {
  return (
    <div className="ui-empty">
      <p className="ui-empty__title">{title}</p>
      {message ? <p className="ui-empty__message">{message}</p> : null}
      {children ? <div className="ui-empty__actions">{children}</div> : null}
    </div>
  )
}

export default EmptyState
