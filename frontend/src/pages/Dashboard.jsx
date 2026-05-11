import { useEffect, useState } from 'react'
import { useUser } from '@clerk/clerk-react'
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer,
} from 'recharts'
import { TrendingUp, TrendingDown, Minus } from 'lucide-react'
import Loading from '../components/Loading'
import Badge from '../components/Badge'
import { api } from '../services/api'
import { statusClasses, statusLabel } from '../utils/status'

function StatCard({ label, value, sub, accent }) {
  const colors = {
    blue:   'from-blue-500 to-blue-600',
    emerald:'from-emerald-500 to-emerald-600',
    violet: 'from-violet-500 to-violet-600',
    amber:  'from-amber-500 to-amber-600',
  }
  return (
    <div className="relative overflow-hidden rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
      <div className={`absolute right-0 top-0 h-24 w-24 translate-x-6 -translate-y-6 rounded-full bg-gradient-to-br opacity-10 ${colors[accent] ?? colors.blue}`} />
      <p className="text-sm font-medium text-slate-500">{label}</p>
      <p className="mt-2 text-3xl font-bold text-slate-900">{value}</p>
      {sub && <p className="mt-1 text-xs text-slate-400">{sub}</p>}
    </div>
  )
}

function CustomTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null
  return (
    <div className="rounded-xl border border-slate-200 bg-white px-4 py-3 shadow-lg">
      <p className="mb-2 text-xs font-semibold text-slate-500">{label}</p>
      {payload.map((p) => (
        <div key={p.name} className="flex items-center gap-2 text-sm">
          <span className="h-2 w-2 rounded-full" style={{ background: p.color }} />
          <span className="text-slate-600">{p.name}:</span>
          <span className="font-semibold text-slate-900">{p.value}</span>
        </div>
      ))}
    </div>
  )
}

function DifficultyRing({ label, solved, total, color }) {
  const pct = total > 0 ? Math.round((solved / total) * 100) : 0
  const styles = {
    easy:   { ring: 'bg-emerald-100', fill: 'bg-emerald-500', text: 'text-emerald-700', badge: 'text-emerald-600' },
    medium: { ring: 'bg-amber-100',   fill: 'bg-amber-500',   text: 'text-amber-700',   badge: 'text-amber-600' },
    hard:   { ring: 'bg-rose-100',    fill: 'bg-rose-500',    text: 'text-rose-700',     badge: 'text-rose-600' },
  }
  const s = styles[color]
  return (
    <div className="flex flex-col items-center gap-3">
      <div className="relative flex h-20 w-20 items-center justify-center">
        <svg className="absolute inset-0 h-full w-full -rotate-90" viewBox="0 0 80 80">
          <circle cx="40" cy="40" r="32" fill="none" stroke="#e2e8f0" strokeWidth="8" />
          <circle
            cx="40" cy="40" r="32" fill="none"
            stroke={color === 'easy' ? '#10b981' : color === 'medium' ? '#f59e0b' : '#f43f5e'}
            strokeWidth="8"
            strokeDasharray={`${2 * Math.PI * 32}`}
            strokeDashoffset={`${2 * Math.PI * 32 * (1 - pct / 100)}`}
            strokeLinecap="round"
            className="transition-all duration-700"
          />
        </svg>
        <div className="text-center">
          <p className="text-lg font-bold text-slate-900">{solved}</p>
        </div>
      </div>
      <div className="text-center">
        <p className={`text-sm font-semibold ${s.text}`}>{label}</p>
        <p className="text-xs text-slate-400">{pct}% solved</p>
      </div>
    </div>
  )
}

function trend(data) {
  if (!data || data.length < 14) return 'neutral'
  const half = Math.floor(data.length / 2)
  const first = data.slice(0, half).reduce((s, d) => s + d.total, 0)
  const second = data.slice(half).reduce((s, d) => s + d.total, 0)
  if (second > first + 1) return 'up'
  if (first > second + 1) return 'down'
  return 'neutral'
}

export default function Dashboard() {
  const { user } = useUser()
  const [dashboard, setDashboard] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!user?.id) return
    api.getDashboard(user.id).then(setDashboard).catch((err) => setError(err.message))
  }, [user?.id])

  if (error) {
    return <div className="rounded-lg border border-rose-200 bg-rose-50 p-5 text-sm text-rose-700">{error}</div>
  }

  if (!dashboard) {
    return <Loading label="Loading dashboard" />
  }

  const direction = trend(dashboard.dailyActivity)
  const TrendIcon = direction === 'up' ? TrendingUp : direction === 'down' ? TrendingDown : Minus
  const trendStyle = direction === 'up'
    ? 'text-emerald-600 bg-emerald-50'
    : direction === 'down'
    ? 'text-rose-600 bg-rose-50'
    : 'text-slate-500 bg-slate-100'
  const trendText = direction === 'up' ? 'Trending up' : direction === 'down' ? 'Trending down' : 'Steady'

  const totalEasy   = 24
  const totalMedium = 47
  const totalHard   = 13

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium uppercase tracking-wide text-slate-500">Overview</p>
          <h2 className="mt-1 text-2xl font-bold text-slate-950">Dashboard</h2>
        </div>
        {user?.firstName && (
          <p className="text-sm text-slate-500">
            Welcome back, <span className="font-semibold text-slate-800">{user.firstName}</span> 👋
          </p>
        )}
      </div>

      {/* Stat cards */}
      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Total Problems" value={dashboard.totalProblems} sub="Available to solve" accent="blue" />
        <StatCard label="Problems Solved" value={dashboard.solvedProblems} sub={`${((dashboard.solvedProblems / dashboard.totalProblems) * 100).toFixed(1)}% of all problems`} accent="emerald" />
        <StatCard label="Total Submissions" value={dashboard.totalSubmissions} sub="All time" accent="violet" />
        <StatCard label="Acceptance Rate" value={`${dashboard.acceptanceRate.toFixed(1)}%`} sub="Accepted / submitted" accent="amber" />
      </section>

      {/* Chart + Difficulty side by side */}
      <div className="grid gap-4 lg:grid-cols-[1fr_auto]">

        {/* Activity chart */}
        <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <div className="mb-5 flex flex-wrap items-start justify-between gap-3">
            <div>
              <h3 className="text-base font-semibold text-slate-900">Submission Activity</h3>
              <p className="text-sm text-slate-400">Last 30 days</p>
            </div>
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-3 text-xs text-slate-500">
                <span className="flex items-center gap-1.5">
                  <span className="h-2.5 w-2.5 rounded-full bg-blue-400" />Total
                </span>
                <span className="flex items-center gap-1.5">
                  <span className="h-2.5 w-2.5 rounded-full bg-emerald-500" />Accepted
                </span>
              </div>
              <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ${trendStyle}`}>
                <TrendIcon size={13} />
                {trendText}
              </span>
            </div>
          </div>

          <ResponsiveContainer width="100%" height={220}>
            <AreaChart data={dashboard.dailyActivity} margin={{ top: 4, right: 4, left: -28, bottom: 0 }}>
              <defs>
                <linearGradient id="gradTotal" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%"   stopColor="#60a5fa" stopOpacity={0.25} />
                  <stop offset="100%" stopColor="#60a5fa" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="gradAccepted" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%"   stopColor="#10b981" stopOpacity={0.3} />
                  <stop offset="100%" stopColor="#10b981" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
              <XAxis
                dataKey="date"
                tick={{ fontSize: 11, fill: '#94a3b8' }}
                tickLine={false}
                axisLine={false}
                interval={4}
              />
              <YAxis
                tick={{ fontSize: 11, fill: '#94a3b8' }}
                tickLine={false}
                axisLine={false}
                allowDecimals={false}
                width={28}
              />
              <Tooltip content={<CustomTooltip />} />
              <Area
                type="monotone"
                dataKey="total"
                stroke="#60a5fa"
                strokeWidth={2}
                fill="url(#gradTotal)"
                dot={false}
                activeDot={{ r: 4, fill: '#60a5fa', strokeWidth: 0 }}
                name="Total"
              />
              <Area
                type="monotone"
                dataKey="accepted"
                stroke="#10b981"
                strokeWidth={2}
                fill="url(#gradAccepted)"
                dot={false}
                activeDot={{ r: 4, fill: '#10b981', strokeWidth: 0 }}
                name="Accepted"
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Difficulty breakdown */}
        <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h3 className="mb-1 text-base font-semibold text-slate-900">Solved by Difficulty</h3>
          <p className="mb-6 text-sm text-slate-400">Unique problems accepted</p>
          <div className="flex flex-col gap-6 sm:flex-row lg:flex-col">
            <DifficultyRing label="Easy"   solved={dashboard.solvedByDifficulty.easy}   total={totalEasy}   color="easy" />
            <DifficultyRing label="Medium" solved={dashboard.solvedByDifficulty.medium} total={totalMedium} color="medium" />
            <DifficultyRing label="Hard"   solved={dashboard.solvedByDifficulty.hard}   total={totalHard}   color="hard" />
          </div>
        </div>
      </div>

      {/* Recent submissions */}
      <section className="rounded-2xl bg-white shadow-sm ring-1 ring-slate-200">
        <div className="border-b border-slate-100 px-6 py-4">
          <h3 className="font-semibold text-slate-950">Recent Submissions</h3>
        </div>
        <div className="divide-y divide-slate-50">
          {dashboard.recentSubmissions.length === 0 ? (
            <p className="px-6 py-8 text-center text-sm text-slate-400">No submissions yet. Start solving problems!</p>
          ) : (
            dashboard.recentSubmissions.map((s) => (
              <div key={s.id} className="flex flex-wrap items-center justify-between gap-3 px-6 py-4 transition-colors hover:bg-slate-50">
                <div>
                  <p className="font-medium text-slate-900">{s.problemTitle}</p>
                  <p className="mt-0.5 text-xs text-slate-400">
                    {new Date(s.submittedAt).toLocaleString()} &nbsp;·&nbsp; {s.language?.toUpperCase()}
                  </p>
                </div>
                <Badge className={statusClasses(s.status)}>{statusLabel(s.status)}</Badge>
              </div>
            ))
          )}
        </div>
      </section>
    </div>
  )
}
