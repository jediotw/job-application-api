import { useEffect, useState, type FormEvent } from 'react'
import { Alert, Button, Input, Label, Textarea } from '../../../components/ui'
import type { Company } from '../../companies/types'
import type { Job } from '../types'
import { toJobRequest, type JobInput } from '../validation'

interface JobFormProps {
  companies: Company[]
  initialJob?: Job | null
  submitting: boolean
  error: string | null
  serverFieldErrors?: Record<string, string>
  onSubmit: (request: ReturnType<typeof toJobRequest>['request']) => Promise<void>
  onCancel?: () => void
}

function initialInput(job?: Job | null): JobInput {
  return {
    companyId: job ? String(job.companyId) : '',
    title: job?.title ?? '',
    description: job?.description ?? '',
    location: job?.location ?? '',
    employmentType: job?.employmentType ?? '',
    salaryMin: job?.salaryMin === null || job?.salaryMin === undefined ? '' : String(job.salaryMin),
    salaryMax: job?.salaryMax === null || job?.salaryMax === undefined ? '' : String(job.salaryMax),
  }
}

function JobForm({
  companies,
  initialJob = null,
  submitting,
  error,
  serverFieldErrors = {},
  onSubmit,
  onCancel,
}: JobFormProps) {
  const [input, setInput] = useState<JobInput>(() => initialInput(initialJob))
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    setInput(initialInput(initialJob))
    setFieldErrors({})
  }, [initialJob])

  function update(field: keyof JobInput, value: string) {
    setInput((current) => ({ ...current, [field]: value }))
    setFieldErrors((current) => ({ ...current, [field]: '' }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const validation = toJobRequest(input)
    if (!validation.request) {
      setFieldErrors(validation.fieldErrors)
      return
    }
    setFieldErrors({})
    await onSubmit(validation.request)
  }

  const errors = { ...serverFieldErrors, ...fieldErrors }

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
      {error ? <Alert variant="error" title="Unable to save job">{error}</Alert> : null}

      <div>
        <Label htmlFor="job-company">Company</Label>
        <select
          id="job-company"
          className="ui-input"
          value={input.companyId}
          onChange={(event) => update('companyId', event.target.value)}
          aria-invalid={Boolean(errors.companyId)}
        >
          <option value="" disabled>Select a company</option>
          {companies.map((company) => (
            <option key={company.id} value={company.id}>{company.name}</option>
          ))}
        </select>
        {errors.companyId ? <p className="field-error">{errors.companyId}</p> : null}
      </div>

      <div>
        <Label htmlFor="job-title">Title</Label>
        <Input id="job-title" value={input.title} onChange={(event) => update('title', event.target.value)} aria-invalid={Boolean(errors.title)} />
        {errors.title ? <p className="field-error">{errors.title}</p> : null}
      </div>

      <div>
        <Label htmlFor="job-description">Description</Label>
        <Textarea id="job-description" value={input.description} onChange={(event) => update('description', event.target.value)} />
        {errors.description ? <p className="field-error">{errors.description}</p> : null}
      </div>

      <div>
        <Label htmlFor="job-location">Location</Label>
        <Input id="job-location" value={input.location} onChange={(event) => update('location', event.target.value)} />
        {errors.location ? <p className="field-error">{errors.location}</p> : null}
      </div>

      <div>
        <Label htmlFor="job-employment-type">Employment type</Label>
        <Input id="job-employment-type" value={input.employmentType} onChange={(event) => update('employmentType', event.target.value)} aria-invalid={Boolean(errors.employmentType)} />
        {errors.employmentType ? <p className="field-error">{errors.employmentType}</p> : null}
      </div>

      <div>
        <Label htmlFor="job-salary-min">Salary minimum</Label>
        <Input id="job-salary-min" type="number" step="any" value={input.salaryMin} onChange={(event) => update('salaryMin', event.target.value)} aria-invalid={Boolean(errors.salaryMin)} />
        {errors.salaryMin ? <p className="field-error">{errors.salaryMin}</p> : null}
      </div>

      <div>
        <Label htmlFor="job-salary-max">Salary maximum</Label>
        <Input id="job-salary-max" type="number" step="any" value={input.salaryMax} onChange={(event) => update('salaryMax', event.target.value)} aria-invalid={Boolean(errors.salaryMax)} />
        {errors.salaryMax ? <p className="field-error">{errors.salaryMax}</p> : null}
      </div>

      <div className="form-actions">
        {onCancel ? <Button variant="ghost" onClick={onCancel} disabled={submitting}>Cancel</Button> : null}
        <Button type="submit" disabled={submitting || companies.length === 0}>
          {submitting ? 'Saving…' : initialJob ? 'Update job' : 'Create job'}
        </Button>
      </div>
    </form>
  )
}

export default JobForm
