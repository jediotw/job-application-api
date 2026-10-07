import { apiClient } from '../../../lib/http'
import { readStoredToken } from '../../auth/session'
import type { CreateJobRequest, Job, UpdateJobRequest } from '../types'

function authHeaders(): Record<string, string> {
  const token = readStoredToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function listJobs(): Promise<Job[]> {
  return apiClient.get<Job[]>('/jobs', { headers: authHeaders() })
}

export function getJob(id: number): Promise<Job> {
  return apiClient.get<Job>(`/jobs/${id}`, { headers: authHeaders() })
}

export function createJob(request: CreateJobRequest): Promise<Job> {
  return apiClient.post<Job>('/jobs', request, { headers: authHeaders() })
}

export function updateJob(id: number, request: UpdateJobRequest): Promise<Job> {
  return apiClient.put<Job>(`/jobs/${id}`, request, { headers: authHeaders() })
}

export function deleteJob(id: number): Promise<void> {
  return apiClient.delete<void>(`/jobs/${id}`, { headers: authHeaders() })
}
