import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { Alert, Button, Card, Input, Label } from '../../../components/ui'
import { HRiringLogo } from '../../../components/branding/HRiringLogo'
import { useAuth } from '../AuthContext'
import { toAuthErrorMessage } from '../errors'

function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  const locationState = location.state as { from?: string; registered?: boolean } | null
  const from = locationState?.from ?? '/dashboard'
  const registered = Boolean(locationState?.registered)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setPending(true)

    try {
      await login({ email, password })
      navigate(from, { replace: true })
    } catch (err) {
      setError(toAuthErrorMessage(err, 'Unable to sign in. Please try again.'))
    } finally {
      setPending(false)
    }
  }

  return (
    <section className="auth-page">
      <HRiringLogo to="/login" />
      <div className="auth-heading">
        <h1>Welcome back</h1>
        <p>Sign in to continue to your HRiring workspace.</p>
      </div>
      <Card title="Sign in">
        {registered ? (
          <Alert variant="success" title="Account created">
            You can sign in with your new account.
          </Alert>
        ) : null}
        {error ? (
          <Alert variant="error" title="Sign in failed">
            {error}
          </Alert>
        ) : null}
        <form className="auth-form" onSubmit={handleSubmit}>
          <div>
            <Label htmlFor="login-email">Email</Label>
            <Input
              id="login-email"
              name="email"
              type="email"
              autoComplete="email"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />
          </div>
          <div>
            <Label htmlFor="login-password">Password</Label>
            <Input
              id="login-password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </div>
          <div>
            <Button type="submit" disabled={pending}>
              {pending ? 'Signing in…' : 'Sign in'}
            </Button>
          </div>
        </form>
        <p className="auth-switch">New to HRiring? <Link to="/register">Create an account</Link></p>
      </Card>
    </section>
  )
}

export default LoginPage
