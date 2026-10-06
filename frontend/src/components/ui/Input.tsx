import type { InputHTMLAttributes } from 'react'

type InputProps = InputHTMLAttributes<HTMLInputElement>

function Input({ type = 'text', className = '', ...rest }: InputProps) {
  return <input type={type} className={['ui-input', className].filter(Boolean).join(' ')} {...rest} />
}

export default Input
