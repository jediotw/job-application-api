import { Button, Card, EmptyState, Loading } from '../../../components/ui'

function DashboardPage() {
  return (
    <section>
      <h1>Dashboard</h1>
      <Card title="Applications">
        <EmptyState
          title="No applications yet"
          message="Applications will appear here once they are submitted."
        >
          <Button variant="secondary">Refresh</Button>
        </EmptyState>
      </Card>
      <Card title="Recent activity">
        <Loading label="Loading activity…" />
      </Card>
    </section>
  )
}

export default DashboardPage
