import type { SavedChecklist } from '../types'

const STORAGE_KEY = 'packingmate.checklist.v1'

export function loadChecklist(): SavedChecklist | null {
  try {
    const serialized = localStorage.getItem(STORAGE_KEY)
    if (!serialized) return null
    const value: unknown = JSON.parse(serialized)
    if (!isSavedChecklist(value)) return null
    return value
  } catch {
    return null
  }
}

export function saveChecklist(value: SavedChecklist | null): void {
  try {
    if (value) localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
    else localStorage.removeItem(STORAGE_KEY)
  } catch {
    // Storage can be disabled or full; the current session remains usable in memory.
  }
}

function isSavedChecklist(value: unknown): value is SavedChecklist {
  if (!value || typeof value !== 'object') return false
  const candidate = value as Partial<SavedChecklist>
  return Boolean(
    candidate.request &&
      candidate.response &&
      Array.isArray(candidate.response.items) &&
      Array.isArray(candidate.customItems) &&
      Array.isArray(candidate.checkedItemIds),
  )
}
