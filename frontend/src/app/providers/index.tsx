import type { ReactNode } from 'react'
import { AuthProvider } from '../../features/auth'

interface AppProvidersProps {
  children: ReactNode
}

function AppProviders({ children }: AppProvidersProps) {
  return <AuthProvider>{children}</AuthProvider>
}

export default AppProviders
