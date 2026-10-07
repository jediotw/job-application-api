import { Button, Card, Input, Label } from '../../../components/ui'

function LoginPage() {
  return (
    <section>
      <h1>Login</h1>
      <Card title="Sign in">
        <div>
          <Label htmlFor="login-email">Email</Label>
          <Input id="login-email" name="email" type="email" autoComplete="email" />
        </div>
        <div>
          <Label htmlFor="login-password">Password</Label>
          <Input
            id="login-password"
            name="password"
            type="password"
            autoComplete="current-password"
          />
        </div>
        <div>
          <Button>Sign in</Button>
        </div>
      </Card>
    </section>
  )
}

export default LoginPage
