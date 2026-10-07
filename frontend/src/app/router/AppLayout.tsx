import { Link, Outlet } from 'react-router-dom'
import { Button } from '../../components/ui'
import { useAuth } from '../../features/auth'

function AppLayout() {
  const { user, isAuthenticated, logout } = useAuth()

  return (
    <div className="layout">
      <header className="layout-header">
        <Link to="/dashboard">Job Application Platform</Link>
        <nav>
          {isAuthenticated ? (
            <>
              <Link to="/dashboard">Dashboard</Link>
              <Link to="/jobs">Jobs</Link>
              {user?.role === 'RECRUITER' ? <Link to="/companies">Companies</Link> : null}
              <Link to="/applications">Applications</Link>
              {user?.role === 'CANDIDATE' ? <Link to="/profile">Profile</Link> : null}
              <span className="layout-user">{user?.email}</span>
              <Button variant="ghost" size="sm" onClick={logout}>
                Logout
              </Button>
            </>
          ) : (
            <>
              <Link to="/login">Login</Link>
              <Link to="/register">Register</Link>
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
