import { Button, Card } from '../../../components/ui'
import type { Candidate } from '../types'

interface CandidateProfileCardProps {
  candidate: Candidate
  onEdit: () => void
}

function formatDate(value: string | null): string {
  if (!value) return '—'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'

  return date.toLocaleString()
}

function ResumeValue({ resumeUrl }: { resumeUrl: string | null }) {
  if (!resumeUrl) return <>—</>

  if (/^https?:\/\//i.test(resumeUrl)) {
    return (
      <a href={resumeUrl} target="_blank" rel="noreferrer noopener">
        {resumeUrl}
      </a>
    )
  }

  return <>{resumeUrl}</>
}

function CandidateProfileCard({ candidate, onEdit }: CandidateProfileCardProps) {
  return (
    <Card title={candidate.name}>
      <dl className="candidate-fields">
        <dt>Email</dt>
        <dd>{candidate.email}</dd>
        <dt>Phone</dt>
        <dd>{candidate.phone || '—'}</dd>
        <dt>Resume</dt>
        <dd>
          <ResumeValue resumeUrl={candidate.resumeUrl} />
        </dd>
        <dt>Profile created</dt>
        <dd>{formatDate(candidate.createdAt)}</dd>
        <dt>Last updated</dt>
        <dd>{formatDate(candidate.updatedAt)}</dd>
      </dl>
      <div className="form-actions">
        <Button variant="secondary" onClick={onEdit}>
          Edit profile
        </Button>
      </div>
    </Card>
  )
}

export default CandidateProfileCard
