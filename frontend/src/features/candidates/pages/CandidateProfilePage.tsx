import { useState } from 'react'
import { Alert, Button, Card, EmptyState, Loading } from '../../../components/ui'
import CandidateForm, { type CandidateFormValues } from '../components/CandidateForm'
import CandidateProfileCard from '../components/CandidateProfileCard'
import { useCandidateProfiles } from '../hooks/useCandidateProfiles'
import type { Candidate, CandidateInput } from '../types'

function toFormValues(candidate: Candidate): CandidateFormValues {
  return {
    name: candidate.name,
    email: candidate.email,
    phone: candidate.phone ?? '',
    resumeUrl: candidate.resumeUrl ?? '',
  }
}

function CandidateProfilePage() {
  const { profiles, loading, error, reload, create, update } = useCandidateProfiles()
  const [creating, setCreating] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  function handleCreate(values: CandidateInput): Promise<void> {
    return create(values).then(() => {
      setCreating(false)
      setNotice('Profile created.')
    })
  }

  function handleUpdate(id: number, values: CandidateInput): Promise<void> {
    return update(id, values).then(() => {
      setEditingId(null)
      setNotice('Profile updated.')
    })
  }

  function handleEdit(candidate: Candidate) {
    setNotice(null)
    setCreating(false)
    setEditingId(candidate.id)
  }

  function handleCancel() {
    setNotice(null)
    setCreating(false)
    setEditingId(null)
  }

  if (loading) {
    return (
      <section>
        <h1>My Profile</h1>
        <Loading label="Loading your profile…" />
      </section>
    )
  }

  if (error) {
    return (
      <section>
        <h1>My Profile</h1>
        <Alert variant="error" title="Unable to load profile">
          {error}
        </Alert>
        <div className="page-actions">
          <Button variant="secondary" onClick={reload}>
            Try again
          </Button>
        </div>
      </section>
    )
  }

  if (creating) {
    return (
      <section>
        <h1>My Profile</h1>
        <Card title="Create your profile">
          <CandidateForm
            submitLabel="Create profile"
            onSubmit={handleCreate}
            onCancel={handleCancel}
          />
        </Card>
      </section>
    )
  }

  if (profiles.length === 0) {
    return (
      <section>
        <h1>My Profile</h1>
        <EmptyState
          title="No profile yet"
          message="Create your profile so recruiters can see your details."
        >
          <Button onClick={() => setCreating(true)}>Create profile</Button>
        </EmptyState>
      </section>
    )
  }

  return (
    <section>
      <h1>My Profile</h1>
      {notice ? (
        <Alert variant="success" title="Saved">
          {notice}
        </Alert>
      ) : null}
      {profiles.map((candidate) =>
        editingId === candidate.id ? (
          <Card key={candidate.id} title="Edit profile">
            <CandidateForm
              initialValues={toFormValues(candidate)}
              submitLabel="Save changes"
              onSubmit={(values) => handleUpdate(candidate.id, values)}
              onCancel={handleCancel}
            />
          </Card>
        ) : (
          <CandidateProfileCard key={candidate.id} candidate={candidate} onEdit={() => handleEdit(candidate)} />
        ),
      )}
    </section>
  )
}

export default CandidateProfilePage
