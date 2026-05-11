export default function MissingConfig() {
  return (
    <main className="min-h-screen bg-slate-950 p-6 text-slate-100">
      <div className="mx-auto mt-24 max-w-xl rounded-lg border border-slate-800 bg-slate-900 p-6">
        <h1 className="text-2xl font-semibold">Clerk key required</h1>
        <p className="mt-3 text-sm text-slate-300">
          Add VITE_CLERK_PUBLISHABLE_KEY to frontend/.env before starting the React app.
        </p>
      </div>
    </main>
  )
}
