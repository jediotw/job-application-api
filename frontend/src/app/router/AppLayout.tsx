import { Link, NavLink, Outlet } from 'react-router-dom'
import { Button } from '../../components/ui'
import { HRiringLogo } from '../../components/branding/HRiringLogo'
import { useAuth } from '../../features/auth'

function navClass({ isActive }: { isActive: boolean }) {
  return 'layout-nav-link' + (isActive ? ' layout-nav-link--active' : '')
}

function NavIcon({ name }: { name: 'dashboard' | 'jobs' | 'applications' | 'companies' | 'profile' }) {
  const paths = {
    dashboard: 'M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h6v6h-6z',
    jobs: 'M5 5h14v14H5zM8 8h8M8 12h8M8 16h5',
    applications: 'M6 3h12a2 2 0 0 1 2 2v14H4V5a2 2 0 0 1 2-2Zm2 4h8M8 11h8M8 15h5',
    companies: 'M5 20V6l7-3 7 3v14M9 20v-3h6v3M8 8h2M14 8h2M8 12h2M14 12h2',
    profile: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-7 8a7 7 0 0 1 14 0',
  } as const

  return (
    <svg className="layout-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d={paths[name]} />
    </svg>
  )
}

function AppLayout() {
  const { user, isAuthenticated, logout } = useAuth()

  if (!isAuthenticated) {
    return (
      <div className="public-layout">
        <header className="public-header">
          <HRiringLogo to="/login" />
          <nav className="public-nav">
            <NavLink className={navClass} to="/login">Sign in</NavLink>
            <NavLink className="public-signup" to="/register">Create account</NavLink>
          </nav>
        </header>
        <main className="public-content">
          <Outlet />
        </main>
        <footer className="site-footer">
          <HRiringLogo to="/login" compact />
          <span>© 2026 HRiring</span>
        </footer>
      </div>
    )
  }

  return (
    <div className="app-shell">
      <aside className="app-sidebar">
        <div className="app-sidebar__brand">
          <HRiringLogo />
        </div>

        <nav className="app-sidebar__nav" aria-label="Primary navigation">
          <span className="app-sidebar__label">Workspace</span>
          <NavLink className={navClass} to="/dashboard"><NavIcon name="dashboard" />Dashboard</NavLink>
          <NavLink className={navClass} to="/jobs"><NavIcon name="jobs" />Jobs</NavLink>
          <NavLink className={navClass} to="/applications"><NavIcon name="applications" />Applications</NavLink>
          {user?.role === 'RECRUITER' ? <NavLink className={navClass} to="/companies"><NavIcon name="companies" />Companies</NavLink> : null}
          {user?.role === 'CANDIDATE' ? <NavLink className={navClass} to="/profile"><NavIcon name="profile" />Profile</NavLink> : null}
        </nav>

        <div className="app-sidebar__footer">
          <div className="app-user">
            <span className="app-user__avatar">{user?.email?.slice(0, 1).toUpperCase()}</span>
            <span className="app-user__details">
              <strong>{user?.role === 'RECRUITER' ? 'Recruiter' : 'Candidate'}</strong>
              <span>{user?.email}</span>
            </span>
          </div>
          <Button variant="ghost" size="sm" onClick={logout}>Sign out</Button>
        </div>
      </aside>

      <div className="app-main">
        <header className="app-topbar">
          <Link className="app-topbar__mobile-brand" to="/dashboard"><HRiringLogo /></Link>
          <div className="app-topbar__spacer" />
          <span className="app-topbar__role">{user?.role === 'RECRUITER' ? 'Recruiter workspace' : 'Candidate workspace'}</span>
        </header>
        <main className="layout-content">
          <Outlet />
        </main>
        <footer className="site-footer">
          <HRiringLogo compact />
          <span>© 2026 HRiring</span>
        </footer>
      </div>
    </div>
  )
}

export default AppLayout
