import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Alert, Button, Card, EmptyState, Loading } from '../../../components/ui'
import { useAuth } from '../../auth'
import { listCompanies } from '../../companies/api'
import type { Company } from '../../companies/types'
import { getJob, listJobs } from '../../jobs/api'
import type { Job } from '../../jobs/types'
import ApplicationCard from '../components/ApplicationCard'
import { toApplicationError } from '../errors'
import { useApplications } from '../hooks/useApplications'
import type { Application } from '../types'

const APPLICATION_STATUS_OPTIONS = ['SHORTLISTED'] as const

function ApplicationDetails({
  application,
  job,
  company,
  recruiter,
  status,
  updating,
  onStatusChange,
  onUpdate,
}: {
  application: Application
  job?: Job
  company?: Company
  recruiter: boolean
  status: string
  updating: boolean
  onStatusChange: (status: string) => void
  onUpdate: () => void
}) {
  return (
    <Card title={job?.title ?? `Application #${application.id}`}>
      <dl className="application-fields">
        <dt>Job</dt><dd>{job?.title ?? `Job #${application.jobId}`}</dd>
        {company ? <><dt>Company</dt><dd>{company.name}</dd></> : null}
        <dt>Status</dt><dd>{application.status}</dd>
        <dt>Applied</dt><dd>{application.appliedAt}</dd>
        <dt>Created</dt><dd>{application.createdAt ?? '—'}</dd>
        <dt>Updated</dt><dd>{application.updatedAt ?? '—'}</dd>
        {recruiter ? <><dt>Candidate</dt><dd>Candidate #{application.candidateId}</dd></> : null}
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
              {status !== application.status ? <option value={status}>{status}</option> : null}
              {APPLICATION_STATUS_OPTIONS.map((option) => (
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
  const [jobs, setJobs] = useState<Job[]>([])
  const [companies, setCompanies] = useState<Company[]>([])
  const [relatedLoading, setRelatedLoading] = useState(true)
  const [relatedError, setRelatedError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setRelatedLoading(true)
    setRelatedError(null)

    Promise.all([listJobs(), listCompanies()])
      .then(([jobResult, companyResult]) => {
        if (cancelled) return
        setJobs(jobResult)
        setCompanies(companyResult)
      })
      .catch((err: unknown) => {
        if (!cancelled) setRelatedError(toApplicationError(err, 'Unable to load job and company information.').message)
      })
      .finally(() => {
        if (!cancelled) setRelatedLoading(false)
      })

    return () => { cancelled = true }
  }, [])

  const jobById = new Map(jobs.map((job) => [job.id, job]))
  const companyById = new Map(companies.map((company) => [company.id, company]))

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
    const job = selectedApplication ? jobById.get(selectedApplication.jobId) : undefined
    const company = job ? companyById.get(job.companyId) : undefined

    return (
      <section>
        <h1>Application details</h1>
        {detailLoading || relatedLoading ? <Loading label="Loading application…" /> : null}
        {detailError ? (
          <>
            <Alert variant="error" title="Unable to load application">{detailError}</Alert>
            <div className="page-actions"><Link to="/applications">Back to applications</Link></div>
          </>
        ) : null}
        {relatedError ? <Alert variant="error" title="Unable to load job details">{relatedError}</Alert> : null}
        {actionError ? <Alert variant="error" title="Application update failed">{actionError}</Alert> : null}
        {notice ? <Alert variant="success" title="Success">{notice}</Alert> : null}
        {!detailLoading && !relatedLoading && !detailError && selectedApplication ? (
          <ApplicationDetails
            application={selectedApplication}
            job={job}
            company={company}
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

  if (loading || relatedLoading) {
    return <section><h1>{isRecruiter ? 'Applications' : 'My Applications'}</h1><Loading label="Loading applications…" /></section>
  }

  if (error) {
    return (
      <section>
        <h1>{isRecruiter ? 'Applications' : 'My Applications'}</h1>
        <Alert variant="error" title="Unable to load applications">{error}</Alert>
        <div className="page-actions"><Button variant="secondary" onClick={reload}>Try again</Button></div>
      </section>
    )
  }

  if (relatedError) {
    return (
      <section>
        <h1>{isRecruiter ? 'Applications' : 'My Applications'}</h1>
        <Alert variant="error" title="Unable to load job details">{relatedError}</Alert>
      </section>
    )
  }

  return (
    <section>
      <h1>{isRecruiter ? 'Applications' : 'My Applications'}</h1>
      {applications.length === 0 ? (
        <EmptyState
          title="No applications"
          message={isRecruiter ? 'Applications for your owned jobs and companies will appear here.' : 'Your submitted applications will appear here.'}
        />
      ) : applications.map((application) => {
        const job = jobById.get(application.jobId)
        const company = job ? companyById.get(job.companyId) : undefined
        return <ApplicationCard key={application.id} application={application} job={job} company={company} />
      })}
    </section>
  )
}

export default ApplicationsPage
