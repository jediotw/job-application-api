import type { CandidateInput } from './types'

const NAME_MAX = 100
const EMAIL_MAX = 255
const PHONE_MAX = 20
const RESUME_URL_MAX = 500

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function validateCandidateInput(values: CandidateInput): Record<string, string> {
  const errors: Record<string, string> = {}

  const name = values.name.trim()
  const email = values.email.trim()
  const phone = values.phone.trim()
  const resumeUrl = values.resumeUrl.trim()

  if (!name) {
    errors.name = 'Name is required.'
  } else if (name.length > NAME_MAX) {
    errors.name = `Name must be ${NAME_MAX} characters or fewer.`
  }

  if (!email) {
    errors.email = 'Email is required.'
  } else if (!EMAIL_PATTERN.test(email)) {
    errors.email = 'Enter a valid email address.'
  } else if (email.length > EMAIL_MAX) {
    errors.email = `Email must be ${EMAIL_MAX} characters or fewer.`
  }

  if (phone.length > PHONE_MAX) {
    errors.phone = `Phone must be ${PHONE_MAX} characters or fewer.`
  }

  if (resumeUrl.length > RESUME_URL_MAX) {
    errors.resumeUrl = `Resume URL must be ${RESUME_URL_MAX} characters or fewer.`
  }

  return errors
}

export function toCandidateInput(values: CandidateInput): CandidateInput {
  return {
    name: values.name.trim(),
    email: values.email.trim(),
    phone: values.phone.trim(),
    resumeUrl: values.resumeUrl.trim(),
  }
}
