import { Button, Card, Input, Label } from '../../../components/ui'

function RegisterPage() {
  return (
    <section>
      <h1>Register</h1>
      <Card title="Create account">
        <div>
          <Label htmlFor="register-email">Email</Label>
          <Input id="register-email" name="email" type="email" autoComplete="email" />
        </div>
        <div>
          <Label htmlFor="register-password">Password</Label>
          <Input
            id="register-password"
            name="password"
            type="password"
            autoComplete="new-password"
          />
        </div>
        <div>
          <Button>Create account</Button>
        </div>
      </Card>
    </section>
  )
}

export default RegisterPage
