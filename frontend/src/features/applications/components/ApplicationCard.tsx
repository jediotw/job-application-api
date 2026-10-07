import { Link } from 'react-router-dom'
import { Card } from '../../../components/ui'
import type { Company } from '../../companies/types'
import type { Job } from '../../jobs/types'
import type { Application } from '../types'

interface ApplicationCardProps {
  application: Application
  job?: Job
  company?: Company
}

function ApplicationCard({ application, job, company }: ApplicationCardProps) {
  return (
    <Card title={job?.title ?? `Application #${application.id}`}>
      <dl className="application-fields">
        <dt>Job</dt>
        <dd>{job?.title ?? `Job #${application.jobId}`}</dd>
        {company ? (
          <>
            <dt>Company</dt>
            <dd>{company.name}</dd>
          </>
        ) : null}
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
