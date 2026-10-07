import { Link } from 'react-router-dom'

type HRiringLogoProps = {
  compact?: boolean
  to?: string
}

export function HRiringLogo({ compact = false, to = '/dashboard' }: HRiringLogoProps) {
  const mark = (
    <img
      className={compact ? 'hriring-logo hriring-logo--compact' : 'hriring-logo'}
      src="/hriring-logo.svg"
      alt="HRiring"
    />
  )

  return to ? <Link className="hriring-brand" to={to} aria-label="HRiring home">{mark}<span>HRiring</span></Link> : mark
}
