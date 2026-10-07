import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Alert, Button, Card, EmptyState, Input, Label, Loading } from '../../../components/ui'
import { useAuth } from '../../auth'
import { listJobs } from '../../jobs/api'
import type { Job } from '../../jobs/types'
import ApplicationCard from '../components/ApplicationCard'
import { toApplicationError } from '../errors'
import { useApplications } from '../hooks/useApplications'
import type { Application } from '../types'

function ApplicationDetails({ application }: { application: Application }) {
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
      <div className="page-actions"><Link to="/applications">Back to applications</Link></div>
    </Card>
  )
}

function ApplicationsPage() {
  const { user } = useAuth()
  const { id } = useParams()
  const { applications, loading, error, reload, create, get } = useApplications()
  const isCandidate = user?.role === 'CANDIDATE'
  const [jobs, setJobs] = useState<Job[]>([])
  const [jobsLoading, setJobsLoading] = useState(isCandidate)
  const [jobsError, setJobsError] = useState<string | null>(null)
  const [status, setStatus] = useState('')
  const [fieldError, setFieldError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [selectedApplication, setSelectedApplication] = useState<Application | null>(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState<string | null>(null)

  useEffect(() => {
    if (!isCandidate) return
    let cancelled = false
    setJobsLoading(true)
    setJobsError(null)
    listJobs()
      .then((result) => { if (!cancelled) setJobs(result) })
      .catch((err: unknown) => {
        if (!cancelled) setJobsError(toApplicationError(err, 'Unable to load jobs.').message)
      })
      .finally(() => { if (!cancelled) setJobsLoading(false) })
    return () => { cancelled = true }
  }, [isCandidate])

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
      .then((result) => { if (!cancelled) setSelectedApplication(result) })
      .catch((err: unknown) => {
        if (!cancelled) setDetailError(toApplicationError(err, 'Unable to load the application.').message)
      })
      .finally(() => { if (!cancelled) setDetailLoading(false) })
    return () => { cancelled = true }
  }, [get, id])

  function handleApply(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFieldError(null)
    setActionError(null)
    setNotice(null)

    const form = new FormData(event.currentTarget)
    const jobId = Number(form.get('jobId'))
    if (!Number.isInteger(jobId) || jobId <= 0) {
      setFieldError('Select a job.')
      return
    }
    if (!status.trim()) {
      setFieldError('Status is required by the application API.')
      return
    }

    setSubmitting(true)
    create({ jobId, status: status.trim() })
      .then(() => {
        setStatus('')
        event.currentTarget.reset()
        setNotice('Application submitted successfully.')
      })
      .catch((err: unknown) => {
        const info = toApplicationError(err, 'Unable to submit the application.')
        setActionError(info.message)
        setFieldError(info.fieldErrors.status ?? null)
      })
      .finally(() => setSubmitting(false))
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
        {!detailLoading && !detailError && selectedApplication ? <ApplicationDetails application={selectedApplication} /> : null}
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
      <h1>{isCandidate ? 'My applications' : 'Applications'}</h1>
      {notice ? <Alert variant="success" title="Success">{notice}</Alert> : null}
      {actionError ? <Alert variant="error" title="Application failed">{actionError}</Alert> : null}

      {isCandidate ? (
        <Card title="Apply for a job">
          {jobsLoading ? <Loading label="Loading available jobs…" /> : null}
          {jobsError ? <Alert variant="error" title="Unable to load jobs">{jobsError}</Alert> : null}
          {!jobsLoading && !jobsError && jobs.length === 0 ? (
            <EmptyState title="No jobs available" message="There are no jobs available to apply for." />
          ) : null}
          {!jobsLoading && !jobsError && jobs.length > 0 ? (
            <form className="auth-form" onSubmit={handleApply}>
              <div>
                <Label htmlFor="application-job">Job</Label>
                <select id="application-job" name="jobId" className="ui-input" defaultValue="">
                  <option value="" disabled>Select a job</option>
                  {jobs.map((job) => <option key={job.id} value={job.id}>#{job.id} — {job.title}</option>)}
                </select>
              </div>
              <div>
                <Label htmlFor="application-status">Status</Label>
                <Input id="application-status" value={status} onChange={(event) => setStatus(event.target.value)} aria-invalid={fieldError !== null} />
                {fieldError ? <p className="field-error">{fieldError}</p> : null}
              </div>
              <div className="form-actions">
                <Button type="submit" disabled={submitting}>{submitting ? 'Submitting…' : 'Apply'}</Button>
              </div>
            </form>
          ) : null}
        </Card>
      ) : null}

      {applications.length === 0 ? (
        <EmptyState
          title="No applications"
          message={isCandidate ? 'Your submitted applications will appear here.' : 'Applications for your owned jobs and companies will appear here.'}
        />
      ) : applications.map((application) => <ApplicationCard key={application.id} application={application} />)}
    </section>
  )
}

export default ApplicationsPage
