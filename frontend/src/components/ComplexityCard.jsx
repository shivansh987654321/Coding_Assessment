const CURVES = {
  'O(1)':        'M0,55 L80,55',
  'O(log N)':    'M0,55 Q20,40 40,25 Q60,15 80,10',
  'O(N)':        'M0,55 L80,5',
  'O(N log N)':  'M0,55 Q30,35 55,15 Q65,8 80,3',
  'O(N²)':       'M0,55 Q40,50 60,25 Q70,10 80,2',
  'O(N^2)':      'M0,55 Q40,50 60,25 Q70,10 80,2',
  'O(2^N)':      'M0,55 Q60,54 70,30 Q75,10 80,1',
}

const COLORS = {
  'O(1)':        '#10b981',
  'O(log N)':    '#10b981',
  'O(N)':        '#f59e0b',
  'O(N log N)':  '#f59e0b',
  'O(N²)':       '#f43f5e',
  'O(N^2)':      '#f43f5e',
  'O(2^N)':      '#f43f5e',
}

function getCurve(complexity) {
  if (!complexity) return CURVES['O(N)']
  const key = Object.keys(CURVES).find(k =>
    complexity.replace(/\s/g, '').toUpperCase() === k.replace(/\s/g, '').toUpperCase()
  )
  return key ? CURVES[key] : CURVES['O(N)']
}

function getColor(complexity) {
  if (!complexity) return '#60a5fa'
  const key = Object.keys(COLORS).find(k =>
    complexity.replace(/\s/g, '').toUpperCase() === k.replace(/\s/g, '').toUpperCase()
  )
  return key ? COLORS[key] : '#60a5fa'
}

export default function ComplexityCard({ analysis }) {
  if (!analysis) return null
  const { timeComplexity, spaceComplexity, suggestion } = analysis
  const color = getColor(timeComplexity)
  const curve = getCurve(timeComplexity)

  return (
    <div className="rounded-xl border border-violet-200 bg-violet-50 p-4 mt-3">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1">
          <p className="text-xs font-semibold uppercase tracking-wide text-violet-500 mb-2">Efficiency</p>
          <div className="space-y-1 text-sm">
            <p className="text-slate-700">
              <span className="text-slate-500">Time complexity: </span>
              <span className="font-semibold" style={{ color }}>{timeComplexity}</span>
            </p>
            <p className="text-slate-700">
              <span className="text-slate-500">Space complexity: </span>
              <span className="font-semibold text-slate-700">{spaceComplexity}</span>
            </p>
            {suggestion && (
              <p className="text-slate-500 text-xs mt-2 leading-relaxed">
                <span className="font-medium text-slate-600">Tip: </span>{suggestion}
              </p>
            )}
          </div>
        </div>
        <div className="shrink-0">
          <svg width="80" height="60" viewBox="0 0 80 60" className="overflow-visible">
            <line x1="0" y1="58" x2="80" y2="58" stroke="#cbd5e1" strokeWidth="1" />
            <line x1="2" y1="0" x2="2" y2="58" stroke="#cbd5e1" strokeWidth="1" />
            <path d={curve} fill="none" stroke={color} strokeWidth="2.5" strokeLinecap="round" />
          </svg>
          <p className="text-center text-xs font-bold mt-1" style={{ color }}>{timeComplexity}</p>
        </div>
      </div>
    </div>
  )
}
