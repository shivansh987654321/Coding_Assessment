const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `Request failed with ${response.status}`)
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}

export const api = {
  syncUser(payload) {
    return request('/users/sync', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  getDashboard(clerkUserId) {
    return request(`/dashboard?clerkUserId=${encodeURIComponent(clerkUserId)}`)
  },
  getProblems() {
    return request('/problems')
  },
  getProblem(id) {
    return request(`/problems/${id}`)
  },
  runCode(payload) {
    return request('/run', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  submitCode(payload) {
    return request('/submit', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  getSubmissions(clerkUserId) {
    return request(`/submissions?clerkUserId=${encodeURIComponent(clerkUserId)}`)
  },
}
