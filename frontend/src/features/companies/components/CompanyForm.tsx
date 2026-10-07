import { useState, type FormEvent } from 'react'
import { Alert, Button, Input, Label, Textarea } from '../../../components/ui'
import { toCompanyError, VALIDATION_FAILED_MESSAGE } from '../errors'
import { toCompanyInput, validateCompanyInput } from '../validation'
import type { Company, CompanyInput } from '../types'

export interface CompanyFormValues {
  name: string
  cin: string
  website: string
  description: string
}

interface CompanyFormProps {
  company?: Company
  submitLabel: string
  onSubmit: (input: CompanyInput) => Promise<void>
  onCancel?: () => void
}

const EMPTY_VALUES: CompanyFormValues = { name: '', cin: '', website: '', description: '' }

function toFormValues(company?: Company): CompanyFormValues {
  if (!company) return EMPTY_VALUES

  return {
    name: company.name,
    cin: company.cin,
    website: company.website ?? '',
    description: company.description ?? '',
  }
}

function CompanyForm({ company, submitLabel, onSubmit, onCancel }: CompanyFormProps) {
  const [values, setValues] = useState<CompanyFormValues>(() => toFormValues(company))
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  function updateField(field: keyof CompanyFormValues, value: string) {
    setValues((current) => ({ ...current, [field]: value }))
    setFieldErrors((current) => {
      if (!(field in current)) return current
      const next = { ...current }
      delete next[field]
      return next
    })
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFormError(null)

    const input = toCompanyInput(values)
    const errors = validateCompanyInput(input)

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    setFieldErrors({})
    setPending(true)

    try {
      await onSubmit(input)
    } catch (err) {
      const info = toCompanyError(err, 'Unable to save the company. Please try again.')
      const hasFieldErrors = Object.keys(info.fieldErrors).length > 0

      setFieldErrors(info.fieldErrors)
      if (!hasFieldErrors || info.message !== VALIDATION_FAILED_MESSAGE) setFormError(info.message)
    } finally {
      setPending(false)
    }
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit} noValidate>
      {formError ? (
        <Alert variant="error" title="Could not save company">
          {formError}
        </Alert>
      ) : null}
      <div>
        <Label htmlFor="company-name">Company name</Label>
        <Input
          id="company-name"
          name="name"
          required
          value={values.name}
          onChange={(event) => updateField('name', event.target.value)}
          aria-invalid={Boolean(fieldErrors.name)}
        />
        {fieldErrors.name ? (
          <p className="field-error" role="alert">
            {fieldErrors.name}
          </p>
        ) : null}
      </div>
      <div>
        <Label htmlFor="company-cin">CIN</Label>
        <Input
          id="company-cin"
          name="cin"
          required
          value={values.cin}
          onChange={(event) => updateField('cin', event.target.value)}
          aria-invalid={Boolean(fieldErrors.cin)}
        />
        {fieldErrors.cin ? (
          <p className="field-error" role="alert">
            {fieldErrors.cin}
          </p>
        ) : null}
      </div>
      <div>
        <Label htmlFor="company-website">Website</Label>
        <Input
          id="company-website"
          name="website"
          type="text"
          placeholder="https://example.com"
          value={values.website}
          onChange={(event) => updateField('website', event.target.value)}
          aria-invalid={Boolean(fieldErrors.website)}
        />
        {fieldErrors.website ? (
          <p className="field-error" role="alert">
            {fieldErrors.website}
          </p>
        ) : null}
      </div>
      <div>
        <Label htmlFor="company-description">Description</Label>
        <Textarea
          id="company-description"
          name="description"
          value={values.description}
          onChange={(event) => updateField('description', event.target.value)}
          aria-invalid={Boolean(fieldErrors.description)}
        />
        {fieldErrors.description ? (
          <p className="field-error" role="alert">
            {fieldErrors.description}
          </p>
        ) : null}
      </div>
      <div className="form-actions">
        <Button type="submit" disabled={pending}>
          {pending ? 'Saving…' : submitLabel}
        </Button>
        {onCancel ? (
          <Button type="button" variant="secondary" onClick={onCancel} disabled={pending}>
            Cancel
          </Button>
        ) : null}
      </div>
    </form>
  )
}

export default CompanyForm
