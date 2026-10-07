export interface Company {
  id: number
  name: string
  cin: string
  website: string | null
  description: string | null
  recruiterId: number
  createdAt: string
  updatedAt: string
}

export interface CreateCompanyRequest {
  name: string
  cin: string
  website?: string | null
  description?: string | null
}

export interface UpdateCompanyRequest {
  name: string
  cin: string
  website?: string | null
  description?: string | null
}

export interface CompanyInput {
  name: string
  cin: string
  website: string
  description: string
}
