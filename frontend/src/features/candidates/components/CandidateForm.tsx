import { useState, type FormEvent } from 'react'
import { Alert, Button, Input, Label } from '../../../components/ui'
import { toCandidateError, VALIDATION_FAILED_MESSAGE } from '../errors'
import { toCandidateInput, validateCandidateInput } from '../validation'
import type { CandidateInput } from '../types'

export interface CandidateFormValues {
  name: string
  email: string
  phone: string
  resumeUrl: string
}

interface CandidateFormProps {
  initialValues?: CandidateFormValues
  submitLabel: string
  onSubmit: (values: CandidateInput) => Promise<void>
  onCancel?: () => void
}

const EMPTY_VALUES: CandidateFormValues = { name: '', email: '', phone: '', resumeUrl: '' }

function CandidateForm({ initialValues, submitLabel, onSubmit, onCancel }: CandidateFormProps) {
  const [values, setValues] = useState<CandidateFormValues>(initialValues ?? EMPTY_VALUES)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  function updateField(field: keyof CandidateFormValues, value: string) {
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

    const input = toCandidateInput(values)
    const errors = validateCandidateInput(input)

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    setFieldErrors({})
    setPending(true)

    try {
      await onSubmit(input)
    } catch (err) {
      const info = toCandidateError(err, 'Unable to save the profile. Please try again.')
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
        <Alert variant="error" title="Could not save profile">
          {formError}
        </Alert>
      ) : null}
      <div>
        <Label htmlFor="candidate-name">Name</Label>
        <Input
          id="candidate-name"
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
        <Label htmlFor="candidate-email">Email</Label>
        <Input
          id="candidate-email"
          name="email"
          type="email"
          autoComplete="email"
          required
          value={values.email}
          onChange={(event) => updateField('email', event.target.value)}
          aria-invalid={Boolean(fieldErrors.email)}
        />
        {fieldErrors.email ? (
          <p className="field-error" role="alert">
            {fieldErrors.email}
          </p>
        ) : null}
      </div>
      <div>
        <Label htmlFor="candidate-phone">Phone</Label>
        <Input
          id="candidate-phone"
          name="phone"
          type="tel"
          autoComplete="tel"
          value={values.phone}
          onChange={(event) => updateField('phone', event.target.value)}
          aria-invalid={Boolean(fieldErrors.phone)}
        />
        {fieldErrors.phone ? (
          <p className="field-error" role="alert">
            {fieldErrors.phone}
          </p>
        ) : null}
      </div>
      <div>
        <Label htmlFor="candidate-resume-url">Resume URL</Label>
        <Input
          id="candidate-resume-url"
          name="resumeUrl"
          value={values.resumeUrl}
          onChange={(event) => updateField('resumeUrl', event.target.value)}
          aria-invalid={Boolean(fieldErrors.resumeUrl)}
        />
        {fieldErrors.resumeUrl ? (
          <p className="field-error" role="alert">
            {fieldErrors.resumeUrl}
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

export default CandidateForm
