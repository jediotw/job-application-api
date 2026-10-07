import { apiClient } from '../../../lib/http'
import { readStoredToken } from '../../auth/session'
import type { Company, CreateCompanyRequest, UpdateCompanyRequest } from '../types'

function authHeaders(): Record<string, string> {
  const token = readStoredToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function listCompanies(): Promise<Company[]> {
  return apiClient.get<Company[]>('/companies', { headers: authHeaders() })
}

export function getCompany(id: number): Promise<Company> {
  return apiClient.get<Company>(`/companies/${id}`, { headers: authHeaders() })
}

export function createCompany(request: CreateCompanyRequest): Promise<Company> {
  return apiClient.post<Company>('/companies', request, { headers: authHeaders() })
}

export function updateCompany(id: number, request: UpdateCompanyRequest): Promise<Company> {
  return apiClient.put<Company>(`/companies/${id}`, request, { headers: authHeaders() })
}

export function deleteCompany(id: number): Promise<void> {
  return apiClient.delete<void>(`/companies/${id}`, { headers: authHeaders() })
}
