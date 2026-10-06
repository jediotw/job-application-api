interface LoadingProps {
  label?: string
}

function Loading({ label = 'Loading…' }: LoadingProps) {
  return (
    <div className="ui-loading" role="status" aria-live="polite">
      <span className="ui-loading__spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  )
}

export default Loading
