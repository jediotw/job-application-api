import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Alert, Button, Card, EmptyState, Loading } from '../../../components/ui'
import { useAuth } from '../../auth'
import ApplicationCard from '../components/ApplicationCard'
import { toApplicationError } from '../errors'
import { useApplications } from '../hooks/useApplications'
import type { Application } from '../types'

const APPLICATION_STATUSES = ['APPLIED', 'SHORTLISTED'] as const

function ApplicationDetails({
  application,
  recruiter,
  status,
  updating,
  onStatusChange,
  onUpdate,
}: {
  application: Application
  recruiter: boolean
  status: string
  updating: boolean
  onStatusChange: (status: string) => void
  onUpdate: () => void
}) {
  return (
    <Card title={`Application #${application.id}`}>
      <dl className="application-fields">
        <dt>Job ID</dt><dd>{application.jobId}</dd>
        <dt>Candidate ID</dt><dd>{application.candidateId}</dd>
        <dt>Status</dt><dd>{application.status}</dd>
        <dt>Applied</dt><dd>{application.appliedAt}</dd>
        <dt>Created</dt><dd>{application.createdAt ?? '—'}</dd>
        <dt>Updated</dt><dd>{application.updatedAt ?? '—'}</dd>
      </dl>
      {recruiter ? (
        <div className="form-actions">
          <div>
            <label htmlFor="application-status">Status</label>
            <select
              id="application-status"
              className="ui-input"
              value={status}
              onChange={(event) => onStatusChange(event.target.value)}
              disabled={updating}
            >
              {APPLICATION_STATUSES.includes(status as (typeof APPLICATION_STATUSES)[number]) ? null : (
                <option value={status}>{status}</option>
              )}
              {APPLICATION_STATUSES.map((option) => (
                <option key={option} value={option}>{option}</option>
              ))}
            </select>
          </div>
          <Button onClick={onUpdate} disabled={updating || status === application.status}>
            {updating ? 'Updating…' : 'Update status'}
          </Button>
        </div>
      ) : null}
      <div className="page-actions"><Link to="/applications">Back to applications</Link></div>
    </Card>
  )
}

function ApplicationsPage() {
  const { user } = useAuth()
  const { id } = useParams()
  const isRecruiter = user?.role === 'RECRUITER'
  const { applications, loading, error, reload, get, update } = useApplications()
  const [status, setStatus] = useState('')
  const [updating, setUpdating] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [selectedApplication, setSelectedApplication] = useState<Application | null>(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) {
      setSelectedApplication(null)
      setDetailError(null)
      return
    }
    const applicationId = Number(id)
    if (!Number.isInteger(applicationId) || applicationId <= 0) {
      setSelectedApplication(null)
      setDetailError('The application ID is invalid.')
      return
    }
    let cancelled = false
    setDetailLoading(true)
    setDetailError(null)
    get(applicationId)
      .then((result) => {
        if (!cancelled) {
          setSelectedApplication(result)
          setStatus(result.status)
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) setDetailError(toApplicationError(err, 'Unable to load the application.').message)
      })
      .finally(() => {
        if (!cancelled) setDetailLoading(false)
      })
    return () => { cancelled = true }
  }, [get, id])

  async function handleUpdate() {
    if (!selectedApplication) return
    setActionError(null)
    setNotice(null)
    setUpdating(true)
    try {
      const updated = await update(selectedApplication.id, { status })
      setSelectedApplication(updated)
      setStatus(updated.status)
      setNotice('Application status updated successfully.')
    } catch (err: unknown) {
      setActionError(toApplicationError(err, 'Unable to update the application status.').message)
    } finally {
      setUpdating(false)
    }
  }

  if (id) {
    return (
      <section>
        <h1>Application details</h1>
        {detailLoading ? <Loading label="Loading application…" /> : null}
        {detailError ? (
          <>
            <Alert variant="error" title="Unable to load application">{detailError}</Alert>
            <div className="page-actions"><Link to="/applications">Back to applications</Link></div>
          </>
        ) : null}
        {actionError ? <Alert variant="error" title="Application update failed">{actionError}</Alert> : null}
        {notice ? <Alert variant="success" title="Success">{notice}</Alert> : null}
        {!detailLoading && !detailError && selectedApplication ? (
          <ApplicationDetails
            application={selectedApplication}
            recruiter={isRecruiter}
            status={status}
            updating={updating}
            onStatusChange={setStatus}
            onUpdate={() => void handleUpdate()}
          />
        ) : null}
      </section>
    )
  }

  if (loading) {
    return <section><h1>Applications</h1><Loading label="Loading applications…" /></section>
  }

  if (error) {
    return (
      <section>
        <h1>Applications</h1>
        <Alert variant="error" title="Unable to load applications">{error}</Alert>
        <div className="page-actions"><Button variant="secondary" onClick={reload}>Try again</Button></div>
      </section>
    )
  }

  return (
    <section>
      <h1>{isRecruiter ? 'Applications' : 'My applications'}</h1>
      {applications.length === 0 ? (
        <EmptyState
          title="No applications"
          message={isRecruiter ? 'Applications for your owned jobs and companies will appear here.' : 'Your submitted applications will appear here.'}
        />
      ) : applications.map((application) => <ApplicationCard key={application.id} application={application} />)}
    </section>
  )
}

export default ApplicationsPage
