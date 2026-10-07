import { useCallback, useEffect, useState } from 'react'
import { createApplication, getApplication, listApplications } from '../api'
import { toApplicationErrorMessage } from '../errors'
import type { Application, CreateApplicationRequest } from '../types'

export interface UseApplicationsResult {
  applications: Application[]
  loading: boolean
  error: string | null
  reload: () => void
  create: (request: CreateApplicationRequest) => Promise<Application>
  get: (id: number) => Promise<Application>
}

export function useApplications(): UseApplicationsResult {
  const [applications, setApplications] = useState<Application[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    listApplications()
      .then((result) => {
        if (cancelled) return
        setApplications(result)
        setLoading(false)
      })
      .catch((err: unknown) => {
        if (cancelled) return
        setError(toApplicationErrorMessage(err, 'Unable to load applications.'))
        setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [reloadKey])

  const reload = useCallback(() => setReloadKey((key) => key + 1), [])

  const create = useCallback(async (request: CreateApplicationRequest) => {
    const created = await createApplication(request)
    setApplications((current) => [...current, created])
    return created
  }, [])

  const get = useCallback((id: number) => getApplication(id), [])

  return { applications, loading, error, reload, create, get }
}
