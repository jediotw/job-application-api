import { useState } from 'react'
import { Alert, Button, Card, EmptyState, Loading } from '../../../components/ui'
import { useAuth } from '../../auth'
import CompanyCard from '../components/CompanyCard'
import CompanyForm from '../components/CompanyForm'
import { toCompanyErrorMessage } from '../errors'
import { useCompanies } from '../hooks/useCompanies'
import type { Company, CompanyInput } from '../types'

function CompaniesPage() {
  const { user } = useAuth()
  const { companies, loading, error, reload, create, update, remove } = useCompanies()
  const isRecruiter = user?.role === 'RECRUITER'

  const [creating, setCreating] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [deletingId, setDeletingId] = useState<number | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  function canManage(company: Company): boolean {
    return isRecruiter && user !== null && company.recruiterId === user.id
  }

  function handleCreate(values: CompanyInput): Promise<void> {
    return create(values).then(() => {
      setCreating(false)
      setNotice('Company created.')
    })
  }

  function handleUpdate(id: number, values: CompanyInput): Promise<void> {
    return update(id, values).then(() => {
      setEditingId(null)
      setNotice('Company updated.')
    })
  }

  function handleStartCreate() {
    setNotice(null)
    setActionError(null)
    setEditingId(null)
    setCreating(true)
  }

  function handleEdit(company: Company) {
    setNotice(null)
    setActionError(null)
    setCreating(false)
    setEditingId(company.id)
  }

  function handleCancel() {
    setNotice(null)
    setActionError(null)
    setCreating(false)
    setEditingId(null)
  }

  function handleDelete(company: Company) {
    const confirmed = window.confirm(`Delete "${company.name}"? This cannot be undone.`)
    if (!confirmed) return

    setNotice(null)
    setActionError(null)
    setDeletingId(company.id)

    remove(company.id)
      .then(() => setNotice('Company deleted.'))
      .catch((err: unknown) =>
        setActionError(toCompanyErrorMessage(err, 'Unable to delete the company.')),
      )
      .finally(() => setDeletingId(null))
  }

  const statusAlerts = (
    <>
      {notice ? (
        <Alert variant="success" title="Done">
          {notice}
        </Alert>
      ) : null}
      {actionError ? (
        <Alert variant="error" title="Action failed">
          {actionError}
        </Alert>
      ) : null}
    </>
  )

  if (loading) {
    return (
      <section>
        <h1>Companies</h1>
        <Loading label="Loading companies…" />
      </section>
    )
  }

  if (error) {
    return (
      <section>
        <h1>Companies</h1>
        <Alert variant="error" title="Unable to load companies">
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
        <h1>Companies</h1>
        <Card title="Add company">
          <CompanyForm
            submitLabel="Create company"
            onSubmit={handleCreate}
            onCancel={handleCancel}
          />
        </Card>
      </section>
    )
  }

  if (companies.length === 0) {
    return (
      <section>
        <h1>Companies</h1>
        {statusAlerts}
        <EmptyState
          title="No companies yet"
          message={
            isRecruiter
              ? 'Add your company so candidates can find your jobs.'
              : 'Companies will appear here once recruiters add them.'
          }
        >
          {isRecruiter ? <Button onClick={handleStartCreate}>Add company</Button> : null}
        </EmptyState>
      </section>
    )
  }

  return (
    <section>
      <h1>Companies</h1>
      {statusAlerts}
      {isRecruiter ? (
        <div className="page-actions">
          <Button onClick={handleStartCreate}>Add company</Button>
        </div>
      ) : null}
      {companies.map((company) =>
        editingId === company.id ? (
          <Card key={company.id} title={`Edit ${company.name}`}>
            <CompanyForm
              company={company}
              submitLabel="Save changes"
              onSubmit={(values) => handleUpdate(company.id, values)}
              onCancel={handleCancel}
            />
          </Card>
        ) : (
          <CompanyCard
            key={company.id}
            company={company}
            canManage={canManage(company)}
            deleting={deletingId === company.id}
            onEdit={() => handleEdit(company)}
            onDelete={() => handleDelete(company)}
          />
        ),
      )}
    </section>
  )
}

export default CompaniesPage
