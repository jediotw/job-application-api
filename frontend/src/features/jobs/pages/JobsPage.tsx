import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { Alert, ApiErrorState, Button, Card, EmptyState, Loading } from '../../../components/ui'
import { listCompanies } from '../../companies/api'
import type { Company } from '../../companies/types'
import { useAuth } from '../../auth'
import { createApplication } from '../../applications/api'
import { toApplicationError } from '../../applications/errors'
import { getJob } from '../api'
import JobCard from '../components/JobCard'
import JobForm from '../components/JobForm'
import { toJobError } from '../errors'
import { useJobs } from '../hooks/useJobs'
import type { Job } from '../types'

function JobsPage() {
  const { user } = useAuth()
  const { id } = useParams()
  const navigate = useNavigate()
  const isRecruiter = user?.role === 'RECRUITER'
  const { jobs, loading, error, reload, create, update, remove } = useJobs()

  const [selectedJob, setSelectedJob] = useState<Job | null>(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState<unknown | null>(null)
  const [companies, setCompanies] = useState<Company[]>([])
  const [companiesLoading, setCompaniesLoading] = useState(isRecruiter)
  const [companiesError, setCompaniesError] = useState<unknown | null>(null)
  const [editingJob, setEditingJob] = useState<Job | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [serverFieldErrors, setServerFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const [deletingId, setDeletingId] = useState<number | null>(null)
  const [applyingJobId, setApplyingJobId] = useState<number | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [formKey, setFormKey] = useState(0)

  useEffect(() => {
    if (!isRecruiter || !user) {
      setCompanies([])
      setCompaniesLoading(false)
      return
    }

    let cancelled = false
    setCompaniesLoading(true)
    setCompaniesError(null)

    listCompanies()
      .then((result) => {
        if (!cancelled) setCompanies(result.filter((company) => company.recruiterId === user.id))
      })
      .catch((err: unknown) => {
        if (!cancelled) setCompaniesError(err)
      })
      .finally(() => {
        if (!cancelled) setCompaniesLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [isRecruiter, user])

  useEffect(() => {
    if (!id) {
      setSelectedJob(null)
      setDetailError(null)
      return
    }

    const jobId = Number(id)
    if (!Number.isInteger(jobId) || jobId <= 0) {
      setSelectedJob(null)
      setDetailError(new Error('The job ID is invalid.'))
      return
    }

    let cancelled = false
    setDetailLoading(true)
    setDetailError(null)

    getJob(jobId)
      .then((result) => {
        if (!cancelled) setSelectedJob(result)
      })
      .catch((err: unknown) => {
        if (!cancelled) setDetailError(err)
      })
      .finally(() => {
        if (!cancelled) setDetailLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [id])

  async function handleApply(jobId: number) {
    setNotice(null)
    setFormError(null)
    setApplyingJobId(jobId)

    try {
      await createApplication({ jobId, status: 'APPLIED' })
      setNotice('Application submitted successfully.')
    } catch (err: unknown) {
      setFormError(toApplicationError(err, 'Unable to submit the application.').message)
    } finally {
      setApplyingJobId(null)
    }
  }

  async function handleCreateOrUpdate(request: Parameters<typeof create>[0]) {
    setSubmitting(true)
    setFormError(null)
    setServerFieldErrors({})

    try {
      if (editingJob) {
        await update(editingJob.id, request)
        setNotice('Job updated successfully.')
        setEditingJob(null)
      } else {
        await create(request)
        setNotice('Job created successfully.')
        setFormKey((key) => key + 1)
      }
    } catch (err: unknown) {
      const info = toJobError(err, 'Unable to save the job.')
      setFormError(info.message)
      setServerFieldErrors(info.fieldErrors)
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(job: Job) {
    if (!window.confirm(`Delete job "${job.title}"?`)) return

    setDeletingId(job.id)
    setNotice(null)
    try {
      await remove(job.id)
      setNotice('Job deleted successfully.')
      if (id && Number(id) === job.id) {
        navigate('/jobs')
      }
      if (editingJob?.id === job.id) {
        setEditingJob(null)
      }
    } catch (err: unknown) {
      const info = toJobError(err, 'Unable to delete the job.')
      setFormError(info.message)
    } finally {
      setDeletingId(null)
    }
  }

  function startEdit(job: Job) {
    setNotice(null)
    setFormError(null)
    setServerFieldErrors({})
    setEditingJob(job)
  }

  if (id) {
    return (
      <section>
        <h1>Job details</h1>
        {detailLoading ? <Loading label="Loading job…" /> : null}
        {detailError ? (
          <>
            <ApiErrorState error={toJobError(detailError, 'Unable to load the job.')} onRetry={() => navigate(`/jobs/${id}`)} />
            <div className="page-actions"><Link to="/jobs">Back to jobs</Link></div>
          </>
        ) : null}
        {!detailLoading && !detailError && selectedJob ? (
          <>
            {notice ? <Alert variant="success" title="Success">{notice}</Alert> : null}
            {formError ? <Alert variant="error" title="Application failed">{formError}</Alert> : null}
            <JobCard
              job={selectedJob}
              recruiter={isRecruiter}
              onApply={!isRecruiter ? () => void handleApply(selectedJob.id) : undefined}
              applying={applyingJobId === selectedJob.id}
              onEdit={isRecruiter ? () => startEdit(selectedJob) : undefined}
              onDelete={isRecruiter ? () => void handleDelete(selectedJob) : undefined}
              deleting={deletingId === selectedJob.id}
            />
            {isRecruiter && editingJob?.id === selectedJob.id ? (
              <Card title="Edit job">
                {companiesLoading ? <Loading label="Loading your companies…" /> : null}
                {companiesError ? <ApiErrorState error={toJobError(companiesError, 'Unable to load your companies.')} /> : null}
                {!companiesLoading && !companiesError ? (
                  <JobForm
                    key={formKey}
                    companies={companies}
                    initialJob={editingJob}
                    submitting={submitting}
                    error={formError}
                    serverFieldErrors={serverFieldErrors}
                    onSubmit={async (request) => {
                      if (request) {
                        await handleCreateOrUpdate(request)
                        setSelectedJob(await getJob(selectedJob.id))
                      }
                    }}
                    onCancel={() => setEditingJob(null)}
                  />
                ) : null}
              </Card>
            ) : null}
            <div className="page-actions"><Link to="/jobs">Back to jobs</Link></div>
          </>
        ) : null}
      </section>
    )
  }

  if (loading) {
    return <section><h1>Jobs</h1><Loading label="Loading jobs…" /></section>
  }

  if (error) {
    return (
      <section>
        <h1>Jobs</h1>
        <ApiErrorState error={toJobError(error, 'Unable to load jobs.')} onRetry={reload} />
      </section>
    )
  }

  return (
    <section>
      <h1>{isRecruiter ? 'Jobs' : 'Available jobs'}</h1>
      {notice ? <Alert variant="success" title="Success">{notice}</Alert> : null}
      {formError && !editingJob ? <Alert variant="error" title="Job action failed">{formError}</Alert> : null}

      {isRecruiter ? (
        <Card title={editingJob ? 'Edit job' : 'Create job'}>
          {companiesLoading ? <Loading label="Loading your companies…" /> : null}
          {companiesError ? <ApiErrorState error={toJobError(companiesError, 'Unable to load your companies.')} /> : null}
          {!companiesLoading && !companiesError && companies.length === 0 ? (
            <EmptyState title="No companies available" message="Create a company before creating a job." />
          ) : null}
          {!companiesLoading && !companiesError && companies.length > 0 ? (
            <JobForm
              key={editingJob ? `edit-${editingJob.id}` : `create-${formKey}`}
              companies={companies}
              initialJob={editingJob}
              submitting={submitting}
              error={formError}
              serverFieldErrors={serverFieldErrors}
              onSubmit={async (request) => {
                if (request) await handleCreateOrUpdate(request)
              }}
              onCancel={editingJob ? () => setEditingJob(null) : undefined}
            />
          ) : null}
        </Card>
      ) : null}

      {jobs.length === 0 ? (
        <EmptyState
          title="No jobs"
          message={isRecruiter ? 'Create your first job to get started.' : 'There are no jobs available right now.'}
        />
      ) : (
        jobs.map((job) => (
          <JobCard
            key={job.id}
            job={job}
            recruiter={isRecruiter}
            onApply={!isRecruiter ? () => void handleApply(job.id) : undefined}
            applying={applyingJobId === job.id}
            onEdit={isRecruiter ? () => startEdit(job) : undefined}
            onDelete={isRecruiter ? () => void handleDelete(job) : undefined}
            deleting={deletingId === job.id}
          />
        ))
      )}
    </section>
  )
}

export default JobsPage
