import type { CompanyInput, CreateCompanyRequest, UpdateCompanyRequest } from './types'

const NAME_MAX = 300
const CIN_MAX = 21
const WEBSITE_MAX = 500

export function validateCompanyInput(values: CompanyInput): Record<string, string> {
  const errors: Record<string, string> = {}

  const name = values.name.trim()
  const cin = values.cin.trim()
  const website = values.website.trim()

  if (!name) {
    errors.name = 'Company name is required.'
  } else if (name.length > NAME_MAX) {
    errors.name = `Company name must be ${NAME_MAX} characters or fewer.`
  }

  if (!cin) {
    errors.cin = 'CIN is required.'
  } else if (/\s/.test(cin)) {
    errors.cin = 'CIN must not contain spaces.'
  } else if (cin.length > CIN_MAX) {
    errors.cin = `CIN must be ${CIN_MAX} characters or fewer.`
  }

  if (website.length > WEBSITE_MAX) {
    errors.website = `Website must be ${WEBSITE_MAX} characters or fewer.`
  } else if (website && /\s/.test(website)) {
    errors.website = 'Website must not contain spaces.'
  } else if (website && !website.includes('.')) {
    errors.website = 'Enter a valid website address.'
  }

  return errors
}

export function toCompanyInput(values: CompanyInput): CompanyInput {
  return {
    name: values.name.trim(),
    cin: values.cin.trim(),
    website: values.website.trim(),
    description: values.description.trim(),
  }
}

export function toCompanyRequest(
  values: CompanyInput,
): CreateCompanyRequest | UpdateCompanyRequest {
  const input = toCompanyInput(values)

  return {
    name: input.name,
    cin: input.cin,
    website: input.website || null,
    description: input.description || null,
  }
}
