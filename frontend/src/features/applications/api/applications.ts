import { apiClient } from '../../../lib/http'
import { readStoredToken } from '../../auth/session'
import type { Application, CreateApplicationRequest, UpdateApplicationRequest } from '../types'

function authHeaders(): Record<string, string> {
  const token = readStoredToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function listApplications(): Promise<Application[]> {
  return apiClient.get<Application[]>('/applications', { headers: authHeaders() })
}

export function getApplication(id: number): Promise<Application> {
  return apiClient.get<Application>(`/applications/${id}`, { headers: authHeaders() })
}

export function createApplication(request: CreateApplicationRequest): Promise<Application> {
  return apiClient.post<Application>('/applications', request, { headers: authHeaders() })
}

export function updateApplication(
  id: number,
  request: UpdateApplicationRequest,
): Promise<Application> {
  return apiClient.put<Application>(`/applications/${id}`, request, { headers: authHeaders() })
}

export function deleteApplication(id: number): Promise<void> {
  return apiClient.delete<void>(`/applications/${id}`, { headers: authHeaders() })
}
