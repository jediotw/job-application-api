import type { TextareaHTMLAttributes } from 'react'

type TextareaProps = TextareaHTMLAttributes<HTMLTextAreaElement>

function Textarea({ className = '', ...rest }: TextareaProps) {
  return (
    <textarea className={['ui-input', 'ui-textarea', className].filter(Boolean).join(' ')} {...rest} />
  )
}

export default Textarea
