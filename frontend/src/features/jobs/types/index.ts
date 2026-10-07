export interface Job {
  id: number
  companyId: number
  title: string
  description: string | null
  location: string | null
  employmentType: string
  salaryMin: number | null
  salaryMax: number | null
  createdAt: string | null
  updatedAt: string | null
}

export interface CreateJobRequest {
  companyId: number
  title: string
  description?: string | null
  location?: string | null
  employmentType: string
  salaryMin?: number | null
  salaryMax?: number | null
}

export interface UpdateJobRequest {
  companyId: number
  title: string
  description?: string | null
  location?: string | null
  employmentType: string
  salaryMin?: number | null
  salaryMax?: number | null
}
