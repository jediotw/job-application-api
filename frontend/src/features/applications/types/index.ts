export interface Application {
  id: number
  candidateId: number
  jobId: number
  status: string
  appliedAt: string
  createdAt: string | null
  updatedAt: string | null
}

export interface CreateApplicationRequest {
  jobId: number
  status: string
}

export interface UpdateApplicationRequest {
  status: string
}
