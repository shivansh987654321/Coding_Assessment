import { Outlet, NavLink, useLocation } from 'react-router-dom'
import { SignOutButton, UserButton } from '@clerk/clerk-react'
import { BarChart3, Code2, LayoutDashboard, LogOut, ListChecks } from 'lucide-react'
import { useBackendUser } from '../hooks/useBackendUser'

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/problems', label: 'Problems', icon: Code2 },
  { to: '/submissions', label: 'Submissions', icon: ListChecks },
]

export default function AppLayout() {
  useBackendUser()
  const location = useLocation()
  const isWorkspace = location.pathname.startsWith('/problem/')

  return (
    <div className={isWorkspace ? 'min-h-screen bg-[#111111]' : 'min-h-screen bg-slate-100'}>
      <header className={`sticky top-0 z-10 border-b ${isWorkspace ? 'border-[#2f2f2f] bg-[#0f0f0f]' : 'border-slate-200 bg-white'}`}>
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-slate-950 text-white">
              <BarChart3 size={20} />
            </div>
            <div>
              <p className={`text-sm font-semibold uppercase tracking-wide ${isWorkspace ? 'text-slate-400' : 'text-slate-500'}`}>CodeAssess</p>
              <h1 className={`text-lg font-semibold ${isWorkspace ? 'text-white' : 'text-slate-950'}`}>Coding Assessment Platform</h1>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <UserButton />
            <SignOutButton>
              <button
                className={`inline-flex h-10 items-center gap-2 rounded-md border px-3 text-sm font-medium ${
                  isWorkspace
                    ? 'border-[#3a3a3a] text-slate-200 hover:bg-[#1f1f1f]'
                    : 'border-slate-200 text-slate-700 hover:bg-slate-50'
                }`}
              >
                <LogOut size={16} />
                Logout
              </button>
            </SignOutButton>
          </div>
        </div>
      </header>

      <div className={isWorkspace ? 'flex gap-0 px-3 py-3' : 'mx-auto flex max-w-7xl gap-6 px-4 py-6 sm:px-6 lg:px-8'}>
        <aside className={`${isWorkspace ? 'hidden' : 'hidden w-64 shrink-0 md:block'}`}>
          <nav className="space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium ${
                      isActive ? 'bg-slate-950 text-white' : 'text-slate-600 hover:bg-white hover:text-slate-950'
                    }`
                  }
                >
                  <Icon size={17} />
                  {item.label}
                </NavLink>
              )
            })}
          </nav>
        </aside>
        <main className="min-w-0 flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
