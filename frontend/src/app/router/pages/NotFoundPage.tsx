import { Link } from 'react-router-dom'
import { Alert } from '../../../components/ui'

function NotFoundPage() {
  return (
    <section>
      <h1>404</h1>
      <Alert variant="error" title="Page not found">
        The page you are looking for does not exist.
      </Alert>
      <Link to="/dashboard">Back to dashboard</Link>
    </section>
  )
}

export default NotFoundPage
