import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Alert, Card, EmptyState, Loading } from '../../../components/ui'
import { useAuth } from '../../../features/auth'
import { getCandidateProfiles } from '../../../features/candidates/api'
import { toCandidateErrorMessage } from '../../../features/candidates/errors'
import { useCompanies } from '../../../features/companies'
import { createApplication } from '../../../features/applications/api'
import ApplicationCard from '../../../features/applications/components/ApplicationCard'
import { toApplicationError } from '../../../features/applications/errors'
import { useApplications } from '../../../features/applications/hooks/useApplications'
import { useJobs } from '../../../features/jobs/hooks/useJobs'
import JobCard from '../../../features/jobs/components/JobCard'
import type { Candidate } from '../../../features/candidates/types'

function DashboardPage() {
  const { user } = useAuth()
  const isRecruiter = user?.role === 'RECRUITER'
  const { jobs, loading: jobsLoading, error: jobsError } = useJobs()
  const { companies, loading: companiesLoading, error: companiesError } = useCompanies()
  const { applications, loading: applicationsLoading, error: applicationsError } = useApplications()
  const [profiles, setProfiles] = useState<Candidate[]>([])
  const [profileLoading, setProfileLoading] = useState(!isRecruiter)
  const [profileError, setProfileError] = useState<string | null>(null)
  const [applyingJobId, setApplyingJobId] = useState<number | null>(null)
  const [applicationNotice, setApplicationNotice] = useState<string | null>(null)
  const [applicationError, setApplicationError] = useState<string | null>(null)

  useEffect(() => {
    if (isRecruiter) {
      setProfileLoading(false)
      return
    }

    let cancelled = false
    setProfileLoading(true)
    setProfileError(null)

    getCandidateProfiles()
      .then((result) => {
        if (!cancelled) setProfiles(result)
      })
      .catch((err: unknown) => {
        if (!cancelled) setProfileError(toCandidateErrorMessage(err, 'Unable to load your profile.'))
      })
      .finally(() => {
        if (!cancelled) setProfileLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [isRecruiter])

  const recruiterCompanies = useMemo(
    () => companies.filter((company) => company.recruiterId === user?.id),
    [companies, user?.id],
  )
  const recruiterCompanyIds = useMemo(
    () => new Set(recruiterCompanies.map((company) => company.id)),
    [recruiterCompanies],
  )
  const visibleJobs = isRecruiter
    ? jobs.filter((job) => recruiterCompanyIds.has(job.companyId))
    : jobs

  async function handleApply(jobId: number) {
    setApplicationNotice(null)
    setApplicationError(null)
    setApplyingJobId(jobId)

    try {
      await createApplication({ jobId, status: 'APPLIED' })
      setApplicationNotice('Application submitted successfully.')
    } catch (err: unknown) {
      setApplicationError(toApplicationError(err, 'Unable to submit the application.').message)
    } finally {
      setApplyingJobId(null)
    }
  }

  if (jobsLoading || companiesLoading || applicationsLoading || profileLoading) {
    return <section><h1>Dashboard</h1><Loading label="Loading dashboard…" /></section>
  }

  if (jobsError || companiesError || applicationsError || profileError) {
    return (
      <section>
        <h1>Dashboard</h1>
        {jobsError ? <Alert variant="error" title="Unable to load jobs">{String(jobsError)}</Alert> : null}
        {companiesError ? <Alert variant="error" title="Unable to load companies">{companiesError}</Alert> : null}
        {applicationsError ? <Alert variant="error" title="Unable to load applications">{applicationsError}</Alert> : null}
        {profileError ? <Alert variant="error" title="Unable to load profile">{profileError}</Alert> : null}
      </section>
    )
  }

  return (
    <section>
      <h1>Dashboard</h1>
      {applicationNotice ? <Alert variant="success" title="Success">{applicationNotice}</Alert> : null}
      {applicationError ? <Alert variant="error" title="Application failed">{applicationError}</Alert> : null}

      {isRecruiter ? (
        <>
          <Card title="My Jobs">
            {visibleJobs.length === 0 ? (
              <EmptyState title="No jobs yet" message="Create a job for one of your companies.">
                <Link to="/jobs">Manage jobs</Link>
              </EmptyState>
            ) : visibleJobs.map((job) => (
              <JobCard key={job.id} job={job} recruiter />
            ))}
          </Card>
          <Card title="My Companies">
            {recruiterCompanies.length === 0 ? (
              <EmptyState title="No companies yet" message="Create your first company.">
                <Link to="/companies">Manage companies</Link>
              </EmptyState>
            ) : recruiterCompanies.map((company) => (
              <Card key={company.id} title={company.name}>
                <p>{company.description || 'No description provided.'}</p>
                <Link to="/companies">Manage companies</Link>
              </Card>
            ))}
          </Card>
          <Card title="Applications">
            {applications.length === 0 ? (
              <EmptyState title="No applications yet" message="Applications for your jobs will appear here.">
                <Link to="/applications">View applications</Link>
              </EmptyState>
            ) : applications.map((application) => (
              <ApplicationCard key={application.id} application={application} />
            ))}
          </Card>
        </>
      ) : (
        <>
          <Card title="Available Jobs">
            {visibleJobs.length === 0 ? (
              <EmptyState title="No jobs available" message="There are no jobs available right now." />
            ) : visibleJobs.map((job) => (
              <JobCard
                key={job.id}
                job={job}
                recruiter={false}
                onApply={() => void handleApply(job.id)}
                applying={applyingJobId === job.id}
              />
            ))}
          </Card>
          <Card title="My Applications">
            {applications.length === 0 ? (
              <EmptyState title="No applications yet" message="Your submitted applications will appear here.">
                <Link to="/applications">View my applications</Link>
              </EmptyState>
            ) : applications.map((application) => (
              <ApplicationCard key={application.id} application={application} />
            ))}
          </Card>
          <Card title="My Profile">
            {profiles.length === 0 ? (
              <EmptyState title="No profile yet" message="Create your candidate profile before applying to jobs.">
                <Link to="/profile">Create profile</Link>
              </EmptyState>
            ) : (
              <p>Your profile is ready. <Link to="/profile">View profile</Link></p>
            )}
          </Card>
        </>
      )}
    </section>
  )
}

export default DashboardPage
