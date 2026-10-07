import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../features/auth'

interface PublicRouteProps {
  children: ReactNode
}

function PublicRoute({ children }: PublicRouteProps) {
  const location = useLocation()
  const { isAuthenticated } = useAuth()

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace state={{ from: location.pathname }} />
  }

  return children
}

export default PublicRoute
