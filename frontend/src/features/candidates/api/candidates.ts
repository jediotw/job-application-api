import { apiClient } from '../../../lib/http'
import type { Candidate, CandidateInput } from '../types'

function toRequestBody(input: CandidateInput) {
  return {
    name: input.name,
    email: input.email,
    phone: input.phone,
    resume_url: input.resumeUrl,
  }
}

export function getCandidateProfiles(): Promise<Candidate[]> {
  return apiClient.get<Candidate[]>('/candidates')
}

export function createCandidate(input: CandidateInput): Promise<Candidate> {
  return apiClient.post<Candidate>('/candidates', toRequestBody(input))
}

export function updateCandidate(id: number, input: CandidateInput): Promise<Candidate> {
  return apiClient.put<Candidate>(`/candidates/${id}`, toRequestBody(input))
}
