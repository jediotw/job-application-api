import type { HTMLAttributes, ReactNode } from 'react'

interface CardProps extends Omit<HTMLAttributes<HTMLElement>, 'title'> {
  title?: ReactNode
  children: ReactNode
}

function Card({ title, className = '', children, ...rest }: CardProps) {
  const classes = ['ui-card', className].filter(Boolean).join(' ')

  return (
    <section className={classes} {...rest}>
      {title ? <h2 className="ui-card__title">{title}</h2> : null}
      <div className="ui-card__body">{children}</div>
    </section>
  )
}

export default Card
