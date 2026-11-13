import type { SavedChecklist, WeatherSummary } from '../types'

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
  if (!isRecord(value) || !isChecklistRequest(value.request) || !isChecklistResponse(value.response)) return false
  return Array.isArray(value.customItems) && value.customItems.every(isChecklistItem) &&
    Array.isArray(value.checkedItemIds) && value.checkedItemIds.every(isString)
}

function isChecklistRequest(value: unknown): boolean {
  return isRecord(value) && isString(value.countryCode) && isString(value.city) &&
    isString(value.departureDate) && isString(value.returnDate) &&
    Number.isInteger(value.travelers) && Number(value.travelers) >= 1 &&
    (value.mode === 'SIMPLE' || value.mode === 'STANDARD') &&
    isStringArray(value.purposes) && isStringArray(value.activities) &&
    isStringArray(value.providedLodgingAmenities) && isStringArray(value.personalSituations)
}

function isChecklistResponse(value: unknown): boolean {
  return isRecord(value) && isString(value.countryCode) &&
    (value.city === null || isString(value.city)) &&
    Number.isInteger(value.tripDays) && Number(value.tripDays) >= 1 &&
    Number.isInteger(value.travelers) && Number(value.travelers) >= 1 &&
    Array.isArray(value.items) && value.items.every(isChecklistItem) &&
    (value.weather === undefined || isWeatherSummary(value.weather))
}

function isChecklistItem(value: unknown): boolean {
  return isRecord(value) && isString(value.itemId) && isString(value.name) &&
    isStringArray(value.aliases) &&
    ['DOCUMENTS_FINANCE', 'CLOTHING', 'TOILETRIES_HYGIENE', 'ELECTRONICS', 'MEDICINE_HEALTH', 'SAFETY_OTHER'].includes(String(value.category)) &&
    (value.scope === 'PERSONAL' || value.scope === 'SHARED') &&
    Number.isInteger(value.quantity) && Number(value.quantity) >= 1 &&
    isStringArray(value.matchedTags) && isString(value.reasonTemplate) &&
    (value.recommendationType === undefined || value.recommendationType === 'BASIC' || value.recommendationType === 'CONDITIONAL') &&
    (value.userAdded === undefined || typeof value.userAdded === 'boolean')
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function isString(value: unknown): value is string {
  return typeof value === 'string'
}

function isStringArray(value: unknown): value is string[] {
  return Array.isArray(value) && value.every(isString)
}

function isWeatherSummary(value: unknown): value is WeatherSummary {
  if (!isRecord(value)) return false
  const source = ['OPEN_METEO', 'NASA_POWER', 'MIXED', 'PARTIAL_OPEN_METEO', 'UNAVAILABLE'].includes(String(value.source))
  const temperatureValid = value.averageTemperatureCelsius === null ||
    (typeof value.averageTemperatureCelsius === 'number' && Number.isFinite(value.averageTemperatureCelsius))
  return source && temperatureValid && typeof value.rainy === 'boolean' &&
    typeof value.uvProtectionRecommended === 'boolean' &&
    Number.isInteger(value.forecastDays) && Number(value.forecastDays) >= 0 &&
    Number.isInteger(value.monthlyAverageDays) && Number(value.monthlyAverageDays) >= 0 &&
    (value.climatologyPeriod === null || isString(value.climatologyPeriod))
}
