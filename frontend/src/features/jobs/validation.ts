import type { CreateJobRequest, UpdateJobRequest } from './types'

export interface JobInput {
  companyId: string
  title: string
  description: string
  location: string
  employmentType: string
  salaryMin: string
  salaryMax: string
}

export interface JobValidation {
  request: CreateJobRequest | UpdateJobRequest | null
  fieldErrors: Record<string, string>
}

export function toJobRequest(input: JobInput): JobValidation {
  const fieldErrors: Record<string, string> = {}
  const companyId = Number(input.companyId)

  if (!Number.isInteger(companyId) || companyId <= 0) {
    fieldErrors.companyId = 'Company is required.'
  }
  if (!input.title.trim()) {
    fieldErrors.title = 'Title is required.'
  }
  if (!input.employmentType.trim()) {
    fieldErrors.employmentType = 'Employment type is required.'
  }

  const salaryMin = input.salaryMin.trim() === '' ? null : Number(input.salaryMin)
  const salaryMax = input.salaryMax.trim() === '' ? null : Number(input.salaryMax)

  if (salaryMin !== null && !Number.isFinite(salaryMin)) {
    fieldErrors.salaryMin = 'Salary minimum must be a valid number.'
  }
  if (salaryMax !== null && !Number.isFinite(salaryMax)) {
    fieldErrors.salaryMax = 'Salary maximum must be a valid number.'
  }

  if (Object.keys(fieldErrors).length > 0) {
    return { request: null, fieldErrors }
  }

  return {
    request: {
      companyId,
      title: input.title.trim(),
      description: input.description.trim() || null,
      location: input.location.trim() || null,
      employmentType: input.employmentType.trim(),
      salaryMin,
      salaryMax,
    },
    fieldErrors: {},
  }
}
