import { useCallback, useEffect, useState } from 'react'
import { createCandidate, getCandidateProfiles, updateCandidate } from '../api'
import { toCandidateErrorMessage } from '../errors'
import type { Candidate, CandidateInput } from '../types'

export interface UseCandidateProfilesResult {
  profiles: Candidate[]
  loading: boolean
  error: string | null
  reload: () => void
  create: (input: CandidateInput) => Promise<Candidate>
  update: (id: number, input: CandidateInput) => Promise<Candidate>
}

export function useCandidateProfiles(): UseCandidateProfilesResult {
  const [profiles, setProfiles] = useState<Candidate[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false

    setLoading(true)
    setError(null)

    getCandidateProfiles()
      .then((result) => {
        if (cancelled) return
        setProfiles(result)
        setLoading(false)
      })
      .catch((err: unknown) => {
        if (cancelled) return
        setError(toCandidateErrorMessage(err, 'Unable to load your profile.'))
        setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [reloadKey])

  const reload = useCallback(() => {
    setReloadKey((key) => key + 1)
  }, [])

  const create = useCallback(async (input: CandidateInput): Promise<Candidate> => {
    const created = await createCandidate(input)
    setProfiles((current) => [...current, created])
    return created
  }, [])

  const update = useCallback(async (id: number, input: CandidateInput): Promise<Candidate> => {
    const updated = await updateCandidate(id, input)
    setProfiles((current) => current.map((item) => (item.id === id ? updated : item)))
    return updated
  }, [])

  return { profiles, loading, error, reload, create, update }
}
