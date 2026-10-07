export interface Candidate {
  id: number
  userId: number
  name: string
  email: string
  phone: string | null
  resumeUrl: string | null
  createdAt: string
  updatedAt: string
}

export interface CandidateInput {
  name: string
  email: string
  phone: string
  resumeUrl: string
}
