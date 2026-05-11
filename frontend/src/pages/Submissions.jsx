import { useEffect, useState } from 'react'
import { useUser } from '@clerk/clerk-react'
import Badge from '../components/Badge'
import Loading from '../components/Loading'
import { api } from '../services/api'
import { statusClasses, statusLabel } from '../utils/status'

export default function Submissions() {
  const { user } = useUser()
  const [submissions, setSubmissions] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!user?.id) return
    api.getSubmissions(user.id).then(setSubmissions).catch((err) => setError(err.message))
  }, [user?.id])

  if (error) {
    return <div className="rounded-lg border border-rose-200 bg-rose-50 p-5 text-sm text-rose-700">{error}</div>
  }

  if (!submissions) {
    return <Loading label="Loading submissions" />
  }

  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-medium uppercase tracking-wide text-slate-500">History</p>
        <h2 className="mt-1 text-2xl font-semibold text-slate-950">Submissions</h2>
      </div>

      <section className="overflow-hidden rounded-lg border border-slate-200 bg-white">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
            <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-5 py-3">Problem</th>
                <th className="px-5 py-3">Status</th>
                <th className="px-5 py-3">Language</th>
                <th className="px-5 py-3">Runtime</th>
                <th className="px-5 py-3">Memory</th>
                <th className="px-5 py-3">Submitted</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {submissions.length === 0 ? (
                <tr>
                  <td colSpan="6" className="px-5 py-5 text-slate-500">
                    No submissions yet.
                  </td>
                </tr>
              ) : (
                submissions.map((submission) => (
                  <tr key={submission.id}>
                    <td className="px-5 py-4 font-medium text-slate-950">{submission.problemTitle}</td>
                    <td className="px-5 py-4">
                      <Badge className={statusClasses(submission.status)}>{statusLabel(submission.status)}</Badge>
                    </td>
                    <td className="px-5 py-4 text-slate-600">{submission.language}</td>
                    <td className="px-5 py-4 text-slate-600">{submission.runtime ?? 0}s</td>
                    <td className="px-5 py-4 text-slate-600">{submission.memory ?? 0} KB</td>
                    <td className="px-5 py-4 text-slate-600">{new Date(submission.submittedAt).toLocaleString()}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  )
}
