import type { LabelHTMLAttributes } from 'react'

type LabelProps = LabelHTMLAttributes<HTMLLabelElement>

function Label({ className = '', children, ...rest }: LabelProps) {
  return (
    <label className={['ui-label', className].filter(Boolean).join(' ')} {...rest}>
      {children}
    </label>
  )
}

export default Label
