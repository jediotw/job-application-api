import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <section>
      <h1>404</h1>
      <p>Page not found</p>
      <Link to="/dashboard">Back to dashboard</Link>
    </section>
  )
}

export default NotFoundPage
