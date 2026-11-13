import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { createChecklist, getCountries } from './api/checklist'
import { loadChecklist, saveChecklist } from './utils/storage'
import type { ChecklistItem, ChecklistRequest, ChecklistResponse, CountryOption, ItemCategory, PowerSummary, WeatherSummary } from './types'
import './App.css'

const categories: { value: ItemCategory; label: string }[] = [
  { value: 'DOCUMENTS_FINANCE', label: '서류·금융' },
  { value: 'CLOTHING', label: '의류' },
  { value: 'TOILETRIES_HYGIENE', label: '세면·위생' },
  { value: 'ELECTRONICS', label: '전자기기' },
  { value: 'MEDICINE_HEALTH', label: '의약·건강' },
  { value: 'SAFETY_OTHER', label: '안전·기타' },
]
const initialRequest: ChecklistRequest = {
  countryCode: '', city: '', departureDate: '', returnDate: '', travelers: 1,
  mode: 'SIMPLE', purposes: [], activities: [], providedLodgingAmenities: [], personalSituations: [],
}
const savedAtStart = loadChecklist()

function App() {
  const [request, setRequest] = useState<ChecklistRequest>(savedAtStart?.request ?? initialRequest)
  const [result, setResult] = useState<ChecklistResponse | null>(savedAtStart?.response ?? null)
  const [checkedIds, setCheckedIds] = useState<Set<string>>(new Set(savedAtStart?.checkedItemIds ?? []))
  const [customItems, setCustomItems] = useState<ChecklistItem[]>(savedAtStart?.customItems ?? [])
  const [step, setStep] = useState(1)
  const [loading, setLoading] = useState(false)
  const [countries, setCountries] = useState<CountryOption[]>([])
  const [countryLoadError, setCountryLoadError] = useState('')
  const [error, setError] = useState('')
  const [newItemName, setNewItemName] = useState('')
  const [newItemScope, setNewItemScope] = useState<'PERSONAL' | 'SHARED'>('PERSONAL')
  const [newItemCategory, setNewItemCategory] = useState<ItemCategory>('SAFETY_OTHER')
  const allItems = useMemo(() => [...(result?.items ?? []), ...customItems], [result, customItems])
  const completedCount = allItems.filter((item) => checkedIds.has(item.itemId)).length
  const responseCountryName = countries.find((country) => country.countryCode === result?.countryCode)?.name

  useEffect(() => {
    let cancelled = false
    getCountries().then((values) => {
      if (!cancelled) setCountries(values)
    }).catch((cause: unknown) => {
      if (!cancelled) setCountryLoadError(cause instanceof Error ? cause.message : '지원 국가 목록을 불러오지 못했습니다.')
    })
    return () => { cancelled = true }
  }, [])

  useEffect(() => {
    if (!result) { saveChecklist(null); return }
    saveChecklist({ request, response: result, customItems, checkedItemIds: [...checkedIds] })
  }, [request, result, customItems, checkedIds])

  function update<K extends keyof ChecklistRequest>(key: K, value: ChecklistRequest[K]) {
    setRequest((current) => ({ ...current, [key]: value }))
  }
  function toggleValue(key: 'purposes' | 'activities' | 'providedLodgingAmenities' | 'personalSituations', value: string) {
    const current = request[key]
    update(key, current.includes(value) ? current.filter((entry) => entry !== value) : [...current, value])
  }
  async function generateChecklist(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(''); setLoading(true)
    try {
      const selectedCountry = countries.find((country) => country.countryCode === request.countryCode)
      if (!selectedCountry) throw new Error('지원 국가를 선택해 주세요.')
      const nextResult = await createChecklist({
        ...request,
        countryCode: selectedCountry.countryCode,
        city: request.city.trim() || selectedCountry.representativeCity,
      })
      const nextIds = new Set(nextResult.items.map((item) => item.itemId))
      setCheckedIds((current) => new Set([...current].filter((id) => nextIds.has(id) || customItems.some((item) => item.itemId === id))))
      setResult(nextResult); setStep(4)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : '체크리스트를 만들지 못했습니다.')
    } finally { setLoading(false) }
  }
  function toggleChecked(itemId: string) {
    setCheckedIds((current) => {
      const next = new Set(current)
      if (next.has(itemId)) {
        next.delete(itemId)
      } else {
        next.add(itemId)
      }
      return next
    })
  }
  function addCustomItem(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const name = newItemName.trim()
    if (!name) return
    setCustomItems((current) => [...current, {
      itemId: `custom-${crypto.randomUUID()}`, name, aliases: [], category: newItemCategory,
      scope: newItemScope, quantity: 1, matchedTags: [], reasonTemplate: '', userAdded: true,
    }])
    setNewItemName('')
  }
  function removeCustomItem(itemId: string) {
    setCustomItems((current) => current.filter((item) => item.itemId !== itemId))
    setCheckedIds((current) => { const next = new Set(current); next.delete(itemId); return next })
  }
  function adjustQuantity(itemId: string, change: number) {
    setResult((current) => current ? ({
      ...current,
      items: current.items.map((item) => item.itemId === itemId
        ? { ...item, quantity: Math.max(1, item.quantity + change) }
        : item),
    }) : current)
  }
  function startOver() {
    setResult(null); setCustomItems([]); setCheckedIds(new Set()); setRequest(initialRequest); setStep(1); setError('')
  }
  const dateRangeInvalid = Boolean(request.departureDate && request.returnDate && request.returnDate <= request.departureDate)

  return <main className="app-shell">
    <header className="topbar"><a className="brand" href="#top" onClick={(event) => event.preventDefault()} aria-label="PackingMate 홈"><span className="brand-mark">P</span><span>PackingMate</span></a><span className="topbar-note">여행 준비를 가볍게</span></header>
    {!result ? <section className="form-layout" id="top">
      <div className="intro-copy"><span className="eyebrow">YOUR TRIP, READY</span><h1>여행 준비,<br /><em>빠짐없이</em> 챙겨요.</h1><p>여행 정보를 알려주시면 필요한 준비물을 정리해 드릴게요.</p><div className="intro-note"><span>✦</span> 항공 여행을 기준으로 체크리스트를 만들어요.</div></div>
      <div className="form-card"><div className="step-header"><div><span className="eyebrow">STEP {Math.min(step, 3)} / 3</span><h2>{step === 1 ? '어디로 떠나세요?' : step === 2 ? '언제, 몇 명이 가나요?' : '어떤 여행인가요?'}</h2></div><div className="step-dots">{[1, 2, 3].map((value) => <span key={value} className={step >= value ? 'active' : ''} />)}</div></div>
        <form onSubmit={step === 3 ? generateChecklist : (event) => { event.preventDefault(); setStep((current) => Math.min(3, current + 1)); setError('') }}>
          {step === 1 && <div className="form-fields">
            <fieldset className="mode-picker"><legend>여행 정보 모드</legend><label className={request.mode === 'SIMPLE' ? 'mode-option selected' : 'mode-option'}><input type="radio" name="mode" checked={request.mode === 'SIMPLE'} onChange={() => update('mode', 'SIMPLE')} /><span><strong>간단 모드</strong><small>목적지와 기본 정보로 만들기</small></span></label><label className={request.mode === 'STANDARD' ? 'mode-option selected' : 'mode-option'}><input type="radio" name="mode" checked={request.mode === 'STANDARD'} onChange={() => update('mode', 'STANDARD')} /><span><strong>표준 모드</strong><small>숙소와 개인 상황도 반영하기</small></span></label></fieldset>
            <label className="field-label" htmlFor="country">목적지 국가 <span>필수 · 5개 지원 국가</span></label><select id="country" className="text-input" required value={request.countryCode} onChange={(event) => update('countryCode', event.target.value)}><option value="" disabled>{countries.length ? '국가를 선택해 주세요' : '국가 목록 불러오는 중…'}</option>{countries.map((country) => <option key={country.countryCode} value={country.countryCode}>{country.name}</option>)}</select>
            {countryLoadError && <p className="inline-error" role="alert">{countryLoadError}</p>}
            <label className="field-label" htmlFor="city">도시 <span>선택</span></label><input id="city" className="text-input" maxLength={100} placeholder={request.countryCode ? `미입력 시 ${countries.find((country) => country.countryCode === request.countryCode)?.representativeCity ?? '대표 도시'} 기준` : '예: 도쿄'} value={request.city} onChange={(event) => update('city', event.target.value)} /><p className="field-hint">도시와 국가 코드는 Open-Meteo 지오코딩에 전달됩니다. 좌표는 Open-Meteo와 NASA POWER 날씨 조회에 사용되며, 여행 날짜는 외부로 전송되지 않습니다. 서버는 여행 입력을 저장하지 않습니다.</p>
          </div>}
          {step === 2 && <div className="form-fields"><div className="date-grid"><label className="field-label" htmlFor="departure">출발일<input id="departure" className="text-input" type="date" required value={request.departureDate} onChange={(event) => update('departureDate', event.target.value)} /></label><label className="field-label" htmlFor="return">귀국일<input id="return" className="text-input" type="date" required min={request.departureDate || undefined} value={request.returnDate} onChange={(event) => update('returnDate', event.target.value)} /></label></div>
            {dateRangeInvalid && <p className="inline-error">귀국일은 출발일보다 뒤여야 해요.</p>}<label className="field-label" htmlFor="travelers">여행 인원</label><div className="number-control"><button type="button" aria-label="인원 줄이기" onClick={() => update('travelers', Math.max(1, request.travelers - 1))}>−</button><output>{request.travelers}명</output><button type="button" aria-label="인원 늘리기" onClick={() => update('travelers', request.travelers + 1)}>+</button></div><p className="field-hint">공동 준비물은 현재 1개로 계산하며, 인원은 안내를 위해 함께 전달됩니다.</p>
          </div>}
          {step === 3 && <div className="form-fields">
            <fieldset className="choice-group"><legend>여행 목적 <span>복수 선택 가능</span></legend><Choice label="휴양·관광" selected={request.purposes.includes('leisure')} onClick={() => toggleValue('purposes', 'leisure')} /><Choice label="출장" selected={request.purposes.includes('business')} onClick={() => toggleValue('purposes', 'business')} /></fieldset>
            <fieldset className="choice-group"><legend>활동 <span>복수 선택 가능</span></legend><Choice label="트레킹" selected={request.activities.includes('trekking')} onClick={() => toggleValue('activities', 'trekking')} /><Choice label="수영" selected={request.activities.includes('swimming')} onClick={() => toggleValue('activities', 'swimming')} /><Choice label="스키" selected={request.activities.includes('ski')} onClick={() => toggleValue('activities', 'ski')} /></fieldset>
            {request.mode === 'STANDARD' && <><fieldset className="choice-group"><legend>숙소에서 제공되는 것</legend><Choice label="헤어드라이어" selected={request.providedLodgingAmenities.includes('hairdryer')} onClick={() => toggleValue('providedLodgingAmenities', 'hairdryer')} /><Choice label="샴푸" selected={request.providedLodgingAmenities.includes('shampoo')} onClick={() => toggleValue('providedLodgingAmenities', 'shampoo')} /></fieldset><fieldset className="choice-group"><legend>해당하는 개인 상황</legend><Choice label="안경·렌즈" selected={request.personalSituations.includes('glasses')} onClick={() => toggleValue('personalSituations', 'glasses')} /><Choice label="카메라" selected={request.personalSituations.includes('camera')} onClick={() => toggleValue('personalSituations', 'camera')} /><Choice label="처방약" selected={request.personalSituations.includes('medication')} onClick={() => toggleValue('personalSituations', 'medication')} /></fieldset><p className="privacy-note">입력 정보는 체크리스트 생성 요청에 사용됩니다. 건강 관련 입력은 약 이름을 받지 않습니다.</p></>}
          </div>}
          {error && <p className="error-banner" role="alert">{error}</p>}<div className="form-actions">{step > 1 && <button type="button" className="secondary-button" onClick={() => { setStep((current) => current - 1); setError('') }}>이전</button>}{step < 3 ? <button type="submit" className="primary-button" disabled={step === 2 && dateRangeInvalid}>다음 단계 <span>→</span></button> : <button type="submit" className="primary-button" disabled={loading || dateRangeInvalid}>{loading ? '체크리스트 만드는 중…' : '체크리스트 만들기'} <span>→</span></button>}</div>
        </form>
      </div>
    </section> : <section className="results-layout" id="top">
      <div className="result-heading"><div><span className="eyebrow">PACKING LIST</span><h1>준비물 체크리스트</h1><p>{result.city ? `${result.city}, ` : ''}{responseCountryName ?? result.countryCode} · {result.tripDays}일 · {result.travelers}명</p></div><button className="secondary-button" type="button" onClick={() => { setResult(null); setStep(1) }}>여행 정보 수정</button></div>
      <div className="progress-panel"><div className="progress-copy"><strong>{completedCount} / {allItems.length} 완료</strong><span>체크 상태는 이 브라우저에 저장돼요.</span></div><div className="progress-track"><span style={{ width: `${allItems.length ? completedCount / allItems.length * 100 : 0}%` }} /></div></div>
      {result.weather && <WeatherNotice weather={result.weather} />}
      {result.power && <PowerNotice power={result.power} />}
      <div className="checklist-columns">{(['PERSONAL', 'SHARED'] as const).map((scope) => { const scopedItems = allItems.filter((item) => item.scope === scope); return <section className="list-section" key={scope}><div className="list-title"><div><span className="eyebrow">{scope === 'PERSONAL' ? 'FOR YOU' : 'SHARE TOGETHER'}</span><h2>{scope === 'PERSONAL' ? '개인용 준비물' : '공동 준비물'}</h2></div><span className="item-count">{scopedItems.length}개</span></div>{scopedItems.length ? <ul className="item-list">{scopedItems.map((item) => <ChecklistRow key={item.itemId} item={item} checked={checkedIds.has(item.itemId)} onToggle={() => toggleChecked(item.itemId)} onRemove={() => removeCustomItem(item.itemId)} onQuantityChange={(change) => adjustQuantity(item.itemId, change)} />)}</ul> : <p className="empty-list">아직 등록된 준비물이 없어요.</p>}</section> })}</div>
      <section className="add-item-panel"><div><span className="eyebrow">MAKE IT YOURS</span><h2>직접 챙길 것도 추가하세요</h2><p>여행 준비물에 나만의 항목을 더할 수 있어요.</p></div><form className="add-item-form" onSubmit={addCustomItem}><label className="sr-only" htmlFor="new-item">추가할 준비물</label><input id="new-item" className="text-input" maxLength={80} placeholder="예: 보조 배터리" value={newItemName} onChange={(event) => setNewItemName(event.target.value)} required /><label className="sr-only" htmlFor="new-category">카테고리</label><select id="new-category" value={newItemCategory} onChange={(event) => setNewItemCategory(event.target.value as ItemCategory)}>{categories.map((category) => <option key={category.value} value={category.value}>{category.label}</option>)}</select><label className="sr-only" htmlFor="new-scope">준비 범위</label><select id="new-scope" value={newItemScope} onChange={(event) => setNewItemScope(event.target.value as 'PERSONAL' | 'SHARED')}><option value="PERSONAL">개인용</option><option value="SHARED">공동용</option></select><button className="primary-button" type="submit">항목 추가 <span>+</span></button></form></section>
      <div className="result-actions"><button type="button" className="text-button" onClick={startOver}>새 체크리스트 만들기</button><span>진행 상태는 현재 기기에 저장됩니다.</span></div>
    </section>}
    <footer className="footer"><span>© PackingMate</span><span>여행 준비를 더 단순하게</span></footer>
  </main>
}

function WeatherNotice({ weather }: { weather: WeatherSummary }) {
  const temperature = weather.averageTemperatureCelsius
  const temperatureText = temperature === null ? '' : `여행 기간 평균 기온 ${temperature.toFixed(1)}°C. `
  const period = weather.climatologyPeriod ? ` (${weather.climatologyPeriod})` : ''
  if (weather.source === 'UNAVAILABLE') {
    return <p className="weather-notice unavailable" role="status">날씨 자료를 확인하지 못해 날씨 조건을 준비물에 반영하지 않았습니다.</p>
  }
  if (weather.source === 'PARTIAL_OPEN_METEO') {
    return <p className="weather-notice partial" role="status">날씨 일부만 반영했습니다. 월평균 대체 자료를 불러오지 못해 Open-Meteo 예보 {weather.forecastDays}일만 사용했습니다. <a href="https://open-meteo.com/en/docs" target="_blank" rel="noreferrer">출처: Open-Meteo</a></p>
  }
  if (weather.source === 'OPEN_METEO') {
    return <p className="weather-notice" role="status">{temperatureText}Open-Meteo 예보 기준입니다. <a href="https://open-meteo.com/en/docs" target="_blank" rel="noreferrer">출처: Open-Meteo</a></p>
  }
  if (weather.source === 'NASA_POWER') {
    return <p className="weather-notice average" role="status">{temperatureText}예보 범위 밖이어서 NASA POWER 월평균 자료로 대체했습니다{period}. <a href="https://power.larc.nasa.gov/docs/services/api/temporal/climatology/" target="_blank" rel="noreferrer">출처: NASA POWER</a></p>
  }
  return <p className="weather-notice average" role="status">{temperatureText}Open-Meteo 예보 {weather.forecastDays}일과 NASA POWER 월평균 {weather.monthlyAverageDays}일을 함께 사용했습니다{period}. <a href="https://open-meteo.com/en/docs" target="_blank" rel="noreferrer">Open-Meteo</a> · <a href="https://power.larc.nasa.gov/docs/services/api/temporal/climatology/" target="_blank" rel="noreferrer">NASA POWER</a></p>
}

function PowerNotice({ power }: { power: PowerSummary }) {
  const suppliesByVoltage = new Map<number, Set<number>>()
  for (const supply of power.supplies) {
    const frequencies = suppliesByVoltage.get(supply.voltage) ?? new Set<number>()
    frequencies.add(supply.frequency)
    suppliesByVoltage.set(supply.voltage, frequencies)
  }
  const supplyText = [...suppliesByVoltage.entries()]
    .map(([voltage, frequencies]) => `${voltage}V · ${[...frequencies].sort((a, b) => a - b).join('/')}Hz`)
    .join(' · ')
  if (!supplyText && power.plugTypes.length === 0) return null
  return <section className="power-notice" role="status" aria-label="목적지 전압과 플러그 안내">
    <p><strong>목적지 전원</strong>{supplyText && ` ${supplyText}`}{power.plugTypes.length > 0 && ` · 플러그 ${power.plugTypes.join(', ')}`}</p>
    {power.plugMismatch && <p>한국에서 사용하는 플러그와 맞지 않을 수 있어 공동용 멀티 어댑터를 추천합니다.</p>}
    {(power.voltageCheckRecommended || power.frequencyCheckRecommended) &&
      <p>목적지 전압이나 주파수가 다를 수 있습니다. 사용 전 기기 라벨의 정격 전압·주파수 범위를 확인하세요.</p>}
    {[...new Set(power.supplies.map((supply) => supply.note).filter(Boolean))].map((note) =>
      <p key={note}>{note}</p>
    )}
    {power.sources.length > 0 && <details><summary>기준 출처와 확인일</summary><ul>{power.sources.map((source) =>
      <li key={`${source.kind}-${source.url}`}><a href={source.url} target="_blank" rel="noreferrer">{source.kind === 'SUPPLY' ? '전압·주파수 출처' : '플러그 출처'}</a> · {source.checkedOn}</li>
    )}</ul></details>}
  </section>
}

function Choice({ label, selected, onClick }: { label: string; selected: boolean; onClick: () => void }) {
  return <button type="button" className={`choice-chip${selected ? ' selected' : ''}`} aria-pressed={selected} onClick={onClick}>{label}<span>{selected ? '✓' : '+'}</span></button>
}
function ChecklistRow({ item, checked, onToggle, onRemove, onQuantityChange }: { item: ChecklistItem; checked: boolean; onToggle: () => void; onRemove: () => void; onQuantityChange: (change: number) => void }) {
  const category = categories.find((entry) => entry.value === item.category)?.label ?? '기타'
  const recommendationType = item.recommendationType ?? (item.matchedTags.length > 0 ? 'CONDITIONAL' : 'BASIC')
  const isConditional = recommendationType === 'CONDITIONAL'
  return <li className={`checklist-row${checked ? ' checked' : ''}`}><label className="check-control"><input type="checkbox" checked={checked} onChange={onToggle} /><span className="custom-check">✓</span><span className="sr-only">{item.name} 챙김 완료</span></label><div className="item-copy"><div className="item-name-line"><strong>{item.name}</strong>{item.userAdded && <span className="item-badge custom-badge">내가 추가</span>}</div><div className="item-meta"><span>{category}</span>{!item.userAdded && isConditional && <span className="item-badge">조건부 추천</span>}</div>{!item.userAdded && isConditional && item.reasonTemplate && <details className="item-reason"><summary>추천 이유 보기</summary><p>{item.reasonTemplate}</p></details>}</div>{!item.userAdded ? <div className="quantity-control"><button type="button" aria-label={`${item.name} 수량 줄이기`} disabled={item.quantity <= 1} onClick={() => onQuantityChange(-1)}>−</button><span>{item.quantity}<small>개</small></span><button type="button" aria-label={`${item.name} 수량 늘리기`} onClick={() => onQuantityChange(1)}>+</button></div> : <div className="quantity">{item.quantity}<small>개</small></div>}{item.userAdded && <button type="button" className="remove-button" aria-label={`${item.name} 삭제`} onClick={onRemove}>×</button>}</li>
}
export default App
