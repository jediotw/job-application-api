import type { ReactNode } from 'react'

export type AlertVariant = 'info' | 'success' | 'warning' | 'error'

interface AlertProps {
  variant?: AlertVariant
  title?: ReactNode
  children?: ReactNode
}

function Alert({ variant = 'info', title, children }: AlertProps) {
  return (
    <div role="alert" className={`ui-alert ui-alert--${variant}`}>
      {title ? <strong className="ui-alert__title">{title}</strong> : null}
      {children ? <span className="ui-alert__body">{children}</span> : null}
    </div>
  )
}

export default Alert
