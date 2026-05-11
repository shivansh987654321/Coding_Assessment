export function statusLabel(status) {
  return String(status || '').replaceAll('_', ' ')
}

export function statusClasses(status) {
  if (status === 'ACCEPTED') {
    return 'bg-emerald-50 text-emerald-700 border-emerald-200'
  }
  if (status === 'WRONG_ANSWER') {
    return 'bg-amber-50 text-amber-700 border-amber-200'
  }
  if (status === 'RUNTIME_ERROR' || status === 'COMPILATION_ERROR') {
    return 'bg-rose-50 text-rose-700 border-rose-200'
  }
  return 'bg-slate-100 text-slate-700 border-slate-200'
}

export function difficultyClasses(difficulty) {
  if (difficulty === 'EASY') {
    return 'bg-emerald-50 text-emerald-700 border-emerald-200'
  }
  if (difficulty === 'MEDIUM') {
    return 'bg-amber-50 text-amber-700 border-amber-200'
  }
  return 'bg-rose-50 text-rose-700 border-rose-200'
}
