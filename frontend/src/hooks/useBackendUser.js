import { useEffect } from 'react'
import { useUser } from '@clerk/clerk-react'
import { api } from '../services/api'

export function getBackendUserPayload(user) {
  return {
    clerkUserId: user?.id,
    name: user?.fullName || user?.username || 'Candidate',
    email: user?.primaryEmailAddress?.emailAddress || '',
  }
}

export function useBackendUser() {
  const { isLoaded, isSignedIn, user } = useUser()

  useEffect(() => {
    if (!isLoaded || !isSignedIn || !user) {
      return
    }
    api.syncUser(getBackendUserPayload(user)).catch((error) => {
      console.error('Failed to sync user', error)
    })
  }, [isLoaded, isSignedIn, user])

  return { user, userPayload: getBackendUserPayload(user) }
}
