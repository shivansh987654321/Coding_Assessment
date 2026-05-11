import { SignUp } from '@clerk/clerk-react'
import { Link } from 'react-router-dom'

export default function Signup() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 p-6">
      <div className="grid w-full max-w-5xl overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm md:grid-cols-[1fr_420px]">
        <section className="flex flex-col justify-between bg-slate-950 p-8 text-white">
          <div>
            <p className="text-sm font-semibold uppercase tracking-wide text-cyan-300">CodeAssess</p>
            <h1 className="mt-5 max-w-md text-4xl font-semibold leading-tight">Create your candidate workspace.</h1>
            <p className="mt-4 max-w-md text-sm leading-6 text-slate-300">
              Track solved problems, submissions, runtime results, and assessment progress from one dashboard.
            </p>
          </div>
          <Link to="/login" className="mt-8 text-sm font-medium text-cyan-200 hover:text-white">
            Already have an account
          </Link>
        </section>
        <section className="flex items-center justify-center p-6">
          <SignUp routing="path" path="/signup" signInUrl="/login" afterSignUpUrl="/dashboard" />
        </section>
      </div>
    </main>
  )
}
