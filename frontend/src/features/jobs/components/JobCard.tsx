import { Link } from 'react-router-dom'
import { Button, Card } from '../../../components/ui'
import type { Job } from '../types'

interface JobCardProps {
  job: Job
  recruiter: boolean
  onEdit?: () => void
  onDelete?: () => void
  deleting?: boolean
  onApply?: () => void
  applying?: boolean
}

function JobCard({
  job,
  recruiter,
  onEdit,
  onDelete,
  deleting = false,
  onApply,
  applying = false,
}: JobCardProps) {
  const salary =
    job.salaryMin !== null || job.salaryMax !== null
      ? `${job.salaryMin ?? '—'} – ${job.salaryMax ?? '—'}`
      : 'Not specified'

  return (
    <Card className="job-card" title={job.title}>
      <dl className="job-fields">
        <dt>Job ID</dt><dd>{job.id}</dd>
        <dt>Company ID</dt><dd>{job.companyId}</dd>
        <dt>Location</dt><dd>{job.location ?? 'Not specified'}</dd>
        <dt>Employment type</dt><dd>{job.employmentType}</dd>
        <dt>Salary</dt><dd>{salary}</dd>
        <dt>Description</dt><dd>{job.description ?? 'Not specified'}</dd>
      </dl>
      <div className="page-actions">
        <Link to={`/jobs/${job.id}`}>View details</Link>
        {!recruiter && onApply ? (
          <Button onClick={onApply} disabled={applying}>
            {applying ? 'Applying…' : 'Apply'}
          </Button>
        ) : null}
        {recruiter && onEdit ? <Button variant="secondary" onClick={onEdit}>Edit</Button> : null}
        {recruiter && onDelete ? (
          <Button variant="ghost" onClick={onDelete} disabled={deleting}>
            {deleting ? 'Deleting…' : 'Delete'}
          </Button>
        ) : null}
      </div>
    </Card>
  )
}

export default JobCard
