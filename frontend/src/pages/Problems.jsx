import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Badge from '../components/Badge'
import Loading from '../components/Loading'
import { api } from '../services/api'
import { difficultyClasses } from '../utils/status'

export default function Problems() {
  const [problems, setProblems] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api.getProblems().then(setProblems).catch((err) => setError(err.message))
  }, [])

  if (error) {
    return <div className="rounded-lg border border-rose-200 bg-rose-50 p-5 text-sm text-rose-700">{error}</div>
  }

  if (!problems) {
    return <Loading label="Loading problems" />
  }

  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-medium uppercase tracking-wide text-slate-500">Problem set</p>
        <h2 className="mt-1 text-2xl font-semibold text-slate-950">Problems</h2>
      </div>

      <section className="grid gap-4">
        {problems.map((problem) => (
          <Link
            key={problem.id}
            to={`/problem/${problem.id}`}
            className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm transition hover:border-slate-300 hover:shadow"
          >
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <h3 className="text-lg font-semibold text-slate-950">{problem.title}</h3>
                <div className="mt-3 flex flex-wrap gap-2">
                  {problem.tags.map((tag) => (
                    <Badge key={tag} className="border-slate-200 bg-slate-50 text-slate-600">
                      {tag}
                    </Badge>
                  ))}
                </div>
              </div>
              <Badge className={difficultyClasses(problem.difficulty)}>{problem.difficulty}</Badge>
            </div>
          </Link>
        ))}
      </section>
    </div>
  )
}
