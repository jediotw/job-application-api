import type { ReactNode } from 'react'
import { AuthProvider, readStoredToken } from '../../features/auth'
import { apiClient } from '../../lib/http'

apiClient.setAuthTokenProvider(readStoredToken)

interface AppProvidersProps {
  children: ReactNode
}

function AppProviders({ children }: AppProvidersProps) {
  return <AuthProvider>{children}</AuthProvider>
}

export default AppProviders
