import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import AppLayout from './AppLayout'
import PublicRoute from './PublicRoute'
import ProtectedRoute from './ProtectedRoute'
import LoginPage from '../../features/auth/pages/LoginPage'
import RegisterPage from '../../features/auth/pages/RegisterPage'
import CandidateProfilePage from '../../features/candidates/pages/CandidateProfilePage'
import CompaniesPage from '../../features/companies/pages/CompaniesPage'
import ApplicationsPage from '../../features/applications/pages/ApplicationsPage'
import DashboardPage from './pages/DashboardPage'
import NotFoundPage from './pages/NotFoundPage'

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route
            path="/login"
            element={
              <PublicRoute>
                <LoginPage />
              </PublicRoute>
            }
          />
          <Route
            path="/register"
            element={
              <PublicRoute>
                <RegisterPage />
              </PublicRoute>
            }
          />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <CandidateProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/applications/:id"
            element={<ProtectedRoute><ApplicationsPage /></ProtectedRoute>}
          />
          <Route
            path="/applications"
            element={<ProtectedRoute><ApplicationsPage /></ProtectedRoute>}
          />
          <Route
            path="/companies"
            element={
              <ProtectedRoute>
                <CompaniesPage />
              </ProtectedRoute>
            }
          />
          <Route path="/404" element={<NotFoundPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default AppRouter
