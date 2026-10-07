import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Alert, Button, Card, Input, Label } from '../../../components/ui'
import { HRiringLogo } from '../../../components/branding/HRiringLogo'
import { useAuth } from '../AuthContext'
import { toAuthErrorMessage } from '../errors'

function RegisterPage() {
  const { register, login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setPending(true)

    try {
      await register({ email, password })
      await login({ email, password })
      navigate('/profile', { replace: true })
    } catch (err) {
      setError(toAuthErrorMessage(err, 'Unable to create the account. Please try again.'))
    } finally {
      setPending(false)
    }
  }

  return (
    <section className="auth-page">
      <HRiringLogo to="/register" />
      <div className="auth-heading">
        <h1>Create your account</h1>
        <p>Join HRiring and manage your job search in one place.</p>
      </div>
      <Card title="Create account">
        {error ? (
          <Alert variant="error" title="Registration failed">
            {error}
          </Alert>
        ) : null}
        <form className="auth-form" onSubmit={handleSubmit}>
          <div>
            <Label htmlFor="register-email">Email</Label>
            <Input
              id="register-email"
              name="email"
              type="email"
              autoComplete="email"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />
          </div>
          <div>
            <Label htmlFor="register-password">Password</Label>
            <Input
              id="register-password"
              name="password"
              type="password"
              autoComplete="new-password"
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </div>
          <div>
            <Button type="submit" disabled={pending}>
              {pending ? 'Creating account…' : 'Create account'}
            </Button>
          </div>
        </form>
        <p className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></p>
      </Card>
    </section>
  )
}

export default RegisterPage
