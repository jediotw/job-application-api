import { useCallback, useEffect, useState } from 'react'
import { createCompany, deleteCompany, listCompanies, updateCompany } from '../api'
import { toCompanyErrorMessage } from '../errors'
import { toCompanyRequest } from '../validation'
import type { Company, CompanyInput } from '../types'

export interface UseCompaniesResult {
  companies: Company[]
  loading: boolean
  error: string | null
  reload: () => void
  create: (input: CompanyInput) => Promise<Company>
  update: (id: number, input: CompanyInput) => Promise<Company>
  remove: (id: number) => Promise<void>
}

export function useCompanies(): UseCompaniesResult {
  const [companies, setCompanies] = useState<Company[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false

    setLoading(true)
    setError(null)

    listCompanies()
      .then((result) => {
        if (cancelled) return
        setCompanies(result)
        setLoading(false)
      })
      .catch((err: unknown) => {
        if (cancelled) return
        setError(toCompanyErrorMessage(err, 'Unable to load companies.'))
        setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [reloadKey])

  const reload = useCallback(() => {
    setReloadKey((key) => key + 1)
  }, [])

  const create = useCallback(async (input: CompanyInput): Promise<Company> => {
    const created = await createCompany(toCompanyRequest(input))
    setCompanies((current) => [...current, created])
    return created
  }, [])

  const update = useCallback(async (id: number, input: CompanyInput): Promise<Company> => {
    const updated = await updateCompany(id, toCompanyRequest(input))
    setCompanies((current) => current.map((item) => (item.id === id ? updated : item)))
    return updated
  }, [])

  const remove = useCallback(async (id: number): Promise<void> => {
    await deleteCompany(id)
    setCompanies((current) => current.filter((item) => item.id !== id))
  }, [])

  return { companies, loading, error, reload, create, update, remove }
}
