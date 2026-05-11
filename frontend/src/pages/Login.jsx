import { SignIn } from '@clerk/clerk-react'
import { Link } from 'react-router-dom'

export default function Login() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 p-6">
      <div className="grid w-full max-w-5xl overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm md:grid-cols-[1fr_420px]">
        <section className="flex flex-col justify-between bg-slate-950 p-8 text-white">
          <div>
            <p className="text-sm font-semibold uppercase tracking-wide text-cyan-300">CodeAssess</p>
            <h1 className="mt-5 max-w-md text-4xl font-semibold leading-tight">Practice, run, and submit coding problems.</h1>
            <p className="mt-4 max-w-md text-sm leading-6 text-slate-300">
              A placement-ready coding assessment MVP with Clerk auth, Monaco editor, Spring Boot APIs, and Judge0 execution.
            </p>
          </div>
          <Link to="/signup" className="mt-8 text-sm font-medium text-cyan-200 hover:text-white">
            Create a new account
          </Link>
        </section>
        <section className="flex items-center justify-center p-6">
          <SignIn routing="path" path="/login" signUpUrl="/signup" afterSignInUrl="/dashboard" />
        </section>
      </div>
    </main>
  )
}
