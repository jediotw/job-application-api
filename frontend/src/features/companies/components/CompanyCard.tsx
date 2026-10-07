import { Button, Card } from '../../../components/ui'
import type { Company } from '../types'

interface CompanyCardProps {
  company: Company
  canManage: boolean
  deleting: boolean
  onEdit: () => void
  onDelete: () => void
}

function formatDate(value: string | null): string {
  if (!value) return '—'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'

  return date.toLocaleString()
}

function WebsiteValue({ website }: { website: string | null }) {
  if (!website) return <>—</>

  const href = /^https?:\/\//i.test(website) ? website : `https://${website}`

  return (
    <a href={href} target="_blank" rel="noreferrer noopener">
      {website}
    </a>
  )
}

function CompanyCard({ company, canManage, deleting, onEdit, onDelete }: CompanyCardProps) {
  return (
    <Card title={company.name}>
      <dl className="company-fields">
        <dt>CIN</dt>
        <dd>{company.cin}</dd>
        <dt>Website</dt>
        <dd>
          <WebsiteValue website={company.website} />
        </dd>
        <dt>Description</dt>
        <dd>{company.description || '—'}</dd>
        <dt>Created</dt>
        <dd>{formatDate(company.createdAt)}</dd>
        <dt>Last updated</dt>
        <dd>{formatDate(company.updatedAt)}</dd>
      </dl>
      {canManage ? (
        <div className="form-actions">
          <Button variant="secondary" onClick={onEdit} disabled={deleting}>
            Edit
          </Button>
          <Button variant="ghost" onClick={onDelete} disabled={deleting}>
            {deleting ? 'Deleting…' : 'Delete'}
          </Button>
        </div>
      ) : null}
    </Card>
  )
}

export default CompanyCard
