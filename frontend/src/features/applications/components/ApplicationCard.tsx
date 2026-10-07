import { Link } from 'react-router-dom'
import { Card } from '../../../components/ui'
import type { Application } from '../types'

interface ApplicationCardProps {
  application: Application
}

function ApplicationCard({ application }: ApplicationCardProps) {
  return (
    <Card title={`Application #${application.id}`}>
      <dl className="application-fields">
        <dt>Job ID</dt>
        <dd>{application.jobId}</dd>
        <dt>Status</dt>
        <dd>{application.status}</dd>
        <dt>Applied</dt>
        <dd>{application.appliedAt}</dd>
      </dl>
      <div className="page-actions">
        <Link to={`/applications/${application.id}`}>View details</Link>
      </div>
    </Card>
  )
}

export default ApplicationCard
