import type { ChecklistRequest, ChecklistResponse, CountryOption } from '../types'

interface ApiError {
  code?: string
  message?: string
  violations?: { field: string; message: string }[]
}

export async function createChecklist(request: ChecklistRequest): Promise<ChecklistResponse> {
  const response = await fetch('/api/v1/checklists', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as ApiError | null
    const details = body?.violations?.map((violation) => violation.message).filter(Boolean).join(' ')
    throw new Error(details || body?.message || '체크리스트를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.')
  }

  return (await response.json()) as ChecklistResponse
}

export async function getCountries(): Promise<CountryOption[]> {
  const response = await fetch('/api/v1/countries')
  if (!response.ok) throw new Error('지원 국가 목록을 불러오지 못했습니다.')
  const countries: unknown = await response.json()
  if (!Array.isArray(countries) || !countries.every(isCountryOption)) {
    throw new Error('지원 국가 목록 응답 형식이 올바르지 않습니다.')
  }
  return countries
}

function isCountryOption(value: unknown): value is CountryOption {
  return typeof value === 'object' && value !== null &&
    typeof (value as CountryOption).countryCode === 'string' &&
    typeof (value as CountryOption).name === 'string' &&
    typeof (value as CountryOption).representativeCity === 'string'
}
