# PackingMate 출처와 규칙

확인일: 2026-11-13

현재 애플리케이션은 날씨 조회에 Open-Meteo와 NASA POWER를 사용합니다. 외부 국가·전압 데이터셋과 Gemini API는 아직 연결하지 않았습니다. 아래 항목은 공식 근거의 적용 상태와 서비스 자체 결정을 구분해 기록합니다.

## 지원 국가 목록과 선정 기준

UI에는 국가 이름을 표시하고, API 경계에서는 ISO 3166-1 alpha-2 코드를 사용합니다. ISO의 전체 국가 코드 목록과 PackingMate가 실제 지원하는 다섯 목적지는 별개의 집합입니다.

| 표시 순서 | 국가 | 코드 | 선정 자료에 기록된 2026년 1~9월 누계 | 도시 미입력 기본값 |
|---:|---|---|---:|---|
| 1 | 일본 | `JP` | 8,262,655명 | 도쿄 |
| 2 | 베트남 | `VN` | 3,058,996명 | 하노이 |
| 3 | 미국 | `US` | 953,665명 | 워싱턴 D.C. |
| 4 | 태국 | `TH` | 840,927명 | 방콕 |
| 5 | 대만 | `TW` | 689,324명 | 타이베이 |

수요 수치는 최종 설계 문서에 적힌 [한국관광공사 목적지 통계](https://datalab.visitkorea.or.kr/site/portal/ex/bbs/View.do?cbIdx=1127&bcIdx=311491) 값에서 가져왔습니다. 이번 코드 변경 중 원자료 파일과 통계 정의는 다시 확인하지 않았습니다. 기본 도시를 각국 수도로 둔 것은 도시 미입력 시 사용할 위치를 정하기 위한 자체 가정입니다.

## 1. 공식 출처로 확인해 적용한 설계·DB·규칙

| 적용 대상 | 근거 | 적용 상태 |
|---|---|---|
| 국가 코드와 지원 국가 (`CountryCatalog`, `/api/v1/countries`, 입력 UI) | [ISO 3166 용어집](https://www.iso.org/glossary-for-iso-3166.html)은 alpha-2를 국가명을 나타내는 두 글자 코드로 설명합니다. 지원 국가 수요 기준은 [한국관광공사 목적지 통계](https://datalab.visitkorea.or.kr/site/portal/ex/bbs/View.do?cbIdx=1127&bcIdx=311491)입니다. | ISO 코드와 서비스 지원 목록을 분리합니다. 초기 지원은 일본(JP), 베트남(VN), 미국(US), 태국(TH), 대만(TW) 다섯 곳이며 서버도 이 목록만 허용합니다. 방문 수는 설계 문서에 기록된 2026년 1~9월 누계값을 사용했으며 이번 코드 변경에서 원자료를 다시 확인하지 않았습니다. |
| 선크림·모자·선글라스 규칙 (`data.sql`) | [WHO 자외선 지침](https://www.who.int/news-room/fact-sheets/detail/ultraviolet-radiation)은 UVI 3 이상에서 자외선 보호를 권고합니다. [Open-Meteo 문서](https://open-meteo.com/en/docs)는 일별 `uv_index_max` 및 시간별 `uv_index` 변수를 제공합니다. | 기온 기준 대신 예보의 일 최고 UVI 또는 NASA POWER 월평균 UVI 3 이상에서 `weather:uv-protection` 태그를 생성합니다. 예보 UVI 기준은 WHO 권고를 참고하며 월평균 UVI 적용은 서비스의 fallback 휴리스틱입니다. |
| 보조 배터리 준비 안내 (`data.sql`) | [인천국제공항의 보조 배터리 안내](https://www.airport.kr/ap_ko/1011/subview.do?enc=Zm5jdDF8QEB8JTJGYmJzJTJGYXBfa28lMkYxNzUlMkYxMzkzNzYlMkZhcnRjbFZpZXcuZG8lM0ZwYWdlJTNEMSUyNmZpbmRUeXBlJTNEJTI2ZmluZFdvcmQlM0QlMjZyZ3NFbmRkZGVTdHIlM0QlMjZyZ3NFbmRkZWVTdHIlM0QlMjY%3D)는 시행일이 있는 기준을 공지합니다. | 변동 가능한 Wh·개수 제한을 시드에 고정하지 않고, 출발 전 공항·항공사의 최신 안내를 확인하라는 문구를 넣었습니다. |

## 2. 추후 연결할 공식 출처

| 작업 | 공식 출처 | 연결할 내용 |
|---|---|---|
| 국가 카탈로그 확장 | [ISO 3166](https://www.iso.org/iso-3166-country-codes.html) | 새 국가를 지원할 때 ISO 할당 코드와 서비스 지원 집합을 구분하고, 해당 국가의 대표 도시·공식 데이터 출처를 확인합니다. |
| 일반 준비물·국가별 여행 규정 DB | [외교부 해외안전여행](https://www.0404.go.kr/) 및 목적지별 정부·공관 안내 | 현재 `data.sql`의 준비물 52개는 수작업 초안이며 외교부 데이터를 복사한 것이 아닙니다. DB 작성 시 항목별로 확인한 출처 URL과 확인일을 저장합니다. 여권·입국·약품 반입 규정은 일반 준비물과 분리해 목적지별 공식 안내를 연결합니다. 홈페이지 주소만 전체 목록의 출처로 취급하지 않습니다. |
| 월평균 기후 fallback 출처 검토 | [기상청 세계 기후평년값](https://data.kma.go.kr/climate/average30Years/selectAverage30YearsWorldList.do?pgmNo=560) | 지점 자료는 사용자가 입력할 수 있는 모든 도시를 보장하지 않아 fallback 제공자로 선택하지 않았습니다. 이번 구현은 NASA POWER의 좌표별 climatology API를 사용합니다. 결정 근거는 `docs/decisions/2026-11-13-weather-fallback-source.md`에 기록했습니다. |
| 실시간 예보·UV | [Open-Meteo Forecast API](https://open-meteo.com/en/docs) | 현재 최대 16일 예보로 구현했습니다. 범위 밖 날짜 및 예보 실패 시 NASA POWER 월평균 자료로 대체하는 것은 서비스 흐름입니다. 무료 API의 비상업 이용 및 CC BY 4.0 조건을 확인했으며 상용 배포 전 최신 이용 조건을 다시 확인해야 합니다. |
| 전압·플러그 | 대상 국가의 공식 전력·표준 기관 또는 검증 가능한 표준기관 자료 | 아직 출처와 국가별 값이 정해지지 않았습니다. 전압·주파수·플러그 타입 및 출처 URL·확인일을 함께 저장합니다. |
| Gemini 추천 | [구조화 출력 문서](https://ai.google.dev/gemini-api/docs/structured-output), [API 호출 한도](https://ai.google.dev/gemini-api/docs/rate-limits) | JSON Schema 일부만 지원되므로 응답을 애플리케이션에서 다시 검증합니다. 모델·계정 등급별 한도는 변할 수 있어 실제 연결 시 최신 문서와 계정 값을 확인합니다. |
| 항공 수하물 안전 안내 | [인천국제공항 안내](https://www.airport.kr/) 및 이용 항공사 공지 | 보조 배터리 등 시점·항공사별 규정은 고정 시드로 단정하지 않고, 사용자에게 최신 안내 확인을 제공하거나 갱신 가능한 데이터로 관리합니다. |

## 3. PackingMate 자체 설계 사항

아래 값은 공식 표준이나 외부 데이터가 아니라 제품 규칙입니다. 현재 동작을 설명하기 위해 남기며, 공식 권고처럼 표시하지 않습니다.

| 적용 위치 | 자체 결정 | 상태 |
|---|---|---|
| `data.sql`, `QuantityRule` | 준비물 52개, 별칭, 태그, 이유 문구, 개인용·공동용 분류 | 수작업 초안. DB를 만들 때 항목별 근거를 확인하고 출처 메타데이터를 추가할 예정입니다. |
| `QuantityRule`, `data.sql` | 속옷·양말은 일수+여분, 상의 2일당 1벌, 하의 3일당 1벌, 소모품 묶음 및 항목별 `max_quantity` | 추천용 휴리스틱입니다. 사용자 조정 가능하며 공식 권장 수량이 아닙니다. |
| `TagGenerator` | `cold <= 10°C`, `hot >= 25°C`, 나머지는 `mild` | 설계 초안에서 정한 기온 분류 휴리스틱입니다. 기상청 위험특보나 보편적 복장 기준이 아닙니다. |
| `ChecklistService` | 출발일과 귀국일을 모두 포함해 여행 일수 계산 | 준비물 수량 계산을 위한 제품 규칙입니다. 외부 표준이 아닙니다. |
| API 입력 검증 | 인원 1명 이상, 도시 최대 100자, 토큰 최대 60자 | 서비스 요청 검증값입니다. 공식 인원·도시·태그 길이 기준은 아닙니다. 근거 없던 인원 상한 20명은 두지 않습니다. |
| 규칙 엔진 | 필요 태그 OR 매칭, 제외 태그 우선, 중복 병합, 공동용 기본 수량 1개 | 제품 요구사항입니다. 공동용 수량은 결과에서 사용자가 조정할 수 있습니다. |
| 규칙 결과 분류 | `requiredTags`가 비어 있으면 기본 항목, 하나 이상이면 조건부 항목입니다. 조건부 항목은 필요 태그 중 하나 이상이 여행 태그와 일치할 때 포함하고, 제외 태그 중 하나라도 여행 태그와 일치하면 기본·조건부 구분 없이 제외합니다. | 서비스는 `recommendationType`으로 유형을 응답하며, 화면은 조건부 항목에만 추천 이유 펼치기를 표시합니다. 제외 태그는 조건부 분류 기준이 아닙니다. |
| 지원 국가 기본 도시 | 일본 도쿄, 베트남 하노이, 미국 워싱턴 D.C., 태국 방콕, 대만 타이베이 | 미입력 도시 fallback을 위한 제품 가정으로 대표 도시를 각국 수도로 정했습니다. 도시명 국가 일치·오타 판정은 지오코딩 연결 후 제공해야 합니다. |
| 결과 UI | 사용자가 조정한 항목 수량은 1 이상, 별도 상한 없음. 사용자 추가 항목은 1개로 시작 | UI 동작입니다. 근거 없던 99개 제한은 제거했습니다. |
| 여행 정보 UI | SIMPLE/STANDARD 모드, 목적·활동·숙소·개인 상황 선택지 | 설계와 시드 태그에 맞춘 제품 선택지입니다. 모든 선택지가 현재 시드 항목을 바꾸는 것은 아닙니다. |

## 아직 연결하지 않은 기능

- 날씨 기능은 연결했습니다. 지오코딩에서 국가 코드를 제한하며 결과가 없는 도시는 재입력을 요청합니다.
- 전압·플러그 기준 데이터와 Gemini API는 아직 연결하지 않았습니다.
- `travel-adapter`에는 국가별 전압·플러그 자료가 없어 매칭 조건이 생성되지 않습니다.
- `activity:long-transfer`, `activity:no-water-activities`는 현재 UI/API에서 생성되지 않습니다. `purpose:business`, `activity:ski`도 준비물 시드에서 사용하는 규칙이 없습니다. 관련 태그 행을 유지·삭제·보완할지 정리가 필요합니다.
- 날씨 연동 후 확인된 출처와 서비스 휴리스틱은 아래 날씨 연동 절에 기록합니다.

## 날씨 연동 (확인일: 2026-11-13)

| 적용 대상 | 출처 | 적용 상태 |
|---|---|---|
| 도시 좌표 및 최대 16일 예보 | [Open-Meteo Geocoding API](https://open-meteo.com/en/docs/geocoding-api), [Forecast API](https://open-meteo.com/en/docs) | 지오코딩 요청에 목적지 ISO alpha-2 국가 코드를 전달해 국가를 제한합니다. 일별 평균 기온, 액체 강수량, 일 최고 UV 지수를 사용합니다. 입력 도시·국가 코드는 Open-Meteo 지오코딩 요청에, 지오코딩 좌표는 예보와 NASA POWER 조회에 사용합니다. 여행 날짜는 서버 내에서만 처리하며 외부로 전송하지 않습니다. |
| 예보 범위 초과 또는 예보 실패 대체 | [NASA POWER Climatology API](https://power.larc.nasa.gov/docs/services/api/temporal/climatology/) | 전 지리 좌표의 월별 평균을 조회합니다. 확인한 현재 응답 기간은 2001년 1월~2025년 12월이며 응답의 기간 값을 API와 화면에 전달합니다. 기상 변수는 MERRA-2, `ALLSKY_SFC_UV_INDEX`는 NASA POWER 응답에 명시된 소스에 따릅니다. |
| 자외선 보호 기준 | [WHO Ultraviolet radiation](https://www.who.int/news-room/fact-sheets/detail/ultraviolet-radiation) | WHO는 UVI 3 이상에서 보호를 권고합니다. 예보는 일 최고 UVI, NASA 월평균 fallback은 월평균 UVI를 사용합니다. 월평균값은 개별 날짜의 최고 UVI 예보와 같지 않으며, 월평균 UVI 3 이상에 따른 항목 추천은 대체 자료용 제품 적용입니다. |
| Open-Meteo 이용 조건 | [Terms of Use](https://open-meteo.com/en/terms) | 확인 당시 무료 API는 비상업적 이용으로 제한되고 CC BY 4.0 조건이 적용됩니다. 상용 배포 전 최신 약관 및 유료 API 필요 여부를 다시 확인해야 합니다. |

KMA 세계기후평년값은 도시별 지점 자료이므로 지원 국가의 대표 도시와 사용자가 입력하는 모든 도시를 포괄하지 못해 이번 fallback 출처로 채택하지 않았습니다. NASA POWER Climatology API는 좌표를 입력해 전 세계 월평균 자료를 반환하므로 지오코딩 좌표에서 동적으로 조회합니다. 기후 자료는 DB에 복사·시드하지 않고 API 응답 기간을 결과에 표시합니다.

날씨가 예보와 fallback을 모두 사용하면 날짜 수로 가중해 여행 기간 평균 기온을 계산합니다. 예보 또는 NASA 월평균 fallback에서 하루 강수량이 1.0mm 이상일 때 `weather:rain`을 생성합니다. 1.0mm 기준은 공식 강수 경보 기준이 아닌 서비스 휴리스틱입니다. NASA 월평균의 `PRECTOTCORR` 단위는 mm/day입니다. 예보의 일 최고 UVI 또는 NASA POWER 월평균 UVI가 3 이상이면 `weather:uv-protection`을 생성합니다. 기존 기온 분류(`cold <= 10°C`, `hot >= 25°C`)는 여행 기간 평균 기온에 적용하는 제품 휴리스틱입니다.

외부 호출 실패 시 규칙 엔진 결과는 반환하며, 날씨 자료가 전혀 없으면 기온·강수·UV 조건 태그를 추가하지 않고 결과에 `UNAVAILABLE`을 표시합니다. 일부 예보만 있고 NASA fallback이 실패하면 확인 가능한 예보 날짜만 적용하고 `PARTIAL_OPEN_METEO`로 표시합니다.
