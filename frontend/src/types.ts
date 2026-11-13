export type ChecklistMode = 'SIMPLE' | 'STANDARD'
export type ItemScope = 'PERSONAL' | 'SHARED'
export type RecommendationType = 'BASIC' | 'CONDITIONAL'
export type WeatherSource = 'OPEN_METEO' | 'NASA_POWER' | 'MIXED' | 'PARTIAL_OPEN_METEO' | 'UNAVAILABLE'

export interface WeatherSummary {
  source: WeatherSource
  averageTemperatureCelsius: number | null
  rainy: boolean
  uvProtectionRecommended: boolean
  forecastDays: number
  monthlyAverageDays: number
  climatologyPeriod: string | null
}
export type ItemCategory =
  | 'DOCUMENTS_FINANCE'
  | 'CLOTHING'
  | 'TOILETRIES_HYGIENE'
  | 'ELECTRONICS'
  | 'MEDICINE_HEALTH'
  | 'SAFETY_OTHER'

export interface CountryOption {
  countryCode: string
  name: string
  representativeCity: string
}

export interface ChecklistRequest {
  countryCode: string
  city: string
  departureDate: string
  returnDate: string
  travelers: number
  mode: ChecklistMode
  purposes: string[]
  activities: string[]
  providedLodgingAmenities: string[]
  personalSituations: string[]
}

export interface ChecklistItem {
  itemId: string
  name: string
  aliases: string[]
  category: ItemCategory
  scope: ItemScope
  quantity: number
  matchedTags: string[]
  reasonTemplate: string
  recommendationType?: RecommendationType
  userAdded?: boolean
}

export interface ChecklistResponse {
  countryCode: string
  city: string | null
  tripDays: number
  travelers: number
  items: ChecklistItem[]
  weather?: WeatherSummary
}

export interface SavedChecklist {
  request: ChecklistRequest
  response: ChecklistResponse
  customItems: ChecklistItem[]
  checkedItemIds: string[]
}
