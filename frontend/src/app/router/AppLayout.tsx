import { Link, NavLink, Outlet } from 'react-router-dom'
import { Button } from '../../components/ui'
import { useAuth } from '../../features/auth'

function navClass({ isActive }: { isActive: boolean }) {
  return 'layout-nav-link' + (isActive ? ' layout-nav-link--active' : '')
}

function AppLayout() {
  const { user, isAuthenticated, logout } = useAuth()

  return (
    <div className="layout">
      <header className="layout-header">
        <Link to="/dashboard">Job Application Platform</Link>
        <nav>
          {isAuthenticated ? (
            <>
              <NavLink className={navClass} to="/dashboard">Dashboard</NavLink>
              <NavLink className={navClass} to="/jobs">Jobs</NavLink>
              {user?.role === 'RECRUITER' ? <NavLink className={navClass} to="/companies">Companies</NavLink> : null}
              <NavLink className={navClass} to="/applications">Applications</NavLink>
              {user?.role === 'CANDIDATE' ? <NavLink className={navClass} to="/profile">Profile</NavLink> : null}
              <span className="layout-user">{user?.email}</span>
              <Button variant="ghost" size="sm" onClick={logout}>Logout</Button>
            </>
          ) : (
            <>
              <NavLink className={navClass} to="/login">Login</NavLink>
              <NavLink className={navClass} to="/register">Register</NavLink>
            </>
          )}
        </nav>
      </header>
      <main className="layout-content">
        <Outlet />
      </main>
    </div>
  )
}

export default AppLayout
