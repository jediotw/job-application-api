import { Link, Outlet } from 'react-router-dom'

function AppLayout() {
  return (
    <div className="layout">
      <header className="layout-header">
        <Link to="/dashboard">Job Application Platform</Link>
        <nav>
          <Link to="/dashboard">Dashboard</Link>
          <Link to="/login">Login</Link>
          <Link to="/register">Register</Link>
        </nav>
      </header>
      <main className="layout-content">
        <Outlet />
      </main>
    </div>
  )
}

export default AppLayout
