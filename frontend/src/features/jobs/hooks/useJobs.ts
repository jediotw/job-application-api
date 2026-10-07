import { useCallback, useEffect, useState } from 'react'
import { createJob, deleteJob, listJobs, updateJob } from '../api'
import type { CreateJobRequest, Job, UpdateJobRequest } from '../types'

export interface UseJobsResult {
  jobs: Job[]
  loading: boolean
  error: unknown | null
  reload: () => void
  create: (request: CreateJobRequest) => Promise<Job>
  update: (id: number, request: UpdateJobRequest) => Promise<Job>
  remove: (id: number) => Promise<void>
}

export function useJobs(): UseJobsResult {
  const [jobs, setJobs] = useState<Job[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<unknown | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    listJobs()
      .then((result) => {
        if (cancelled) return
        setJobs(result)
        setLoading(false)
      })
      .catch((err: unknown) => {
        if (cancelled) return
        setError(err)
        setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [reloadKey])

  const reload = useCallback(() => setReloadKey((key) => key + 1), [])

  const create = useCallback(async (request: CreateJobRequest) => {
    const created = await createJob(request)
    setJobs((current) => [...current, created])
    return created
  }, [])

  const update = useCallback(async (id: number, request: UpdateJobRequest) => {
    const updated = await updateJob(id, request)
    setJobs((current) => current.map((job) => (job.id === id ? updated : job)))
    return updated
  }, [])

  const remove = useCallback(async (id: number) => {
    await deleteJob(id)
    setJobs((current) => current.filter((job) => job.id !== id))
  }, [])

  return { jobs, loading, error, reload, create, update, remove }
}
