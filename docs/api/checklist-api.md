# 체크리스트 생성 API 계약 (v1)

## `POST /api/v1/checklists`

JSON 요청을 검증하고 현재 DB의 준비물 규칙과 규칙 엔진으로 추천 항목을 반환합니다. 날짜는 ISO-8601(`YYYY-MM-DD`)이며 출발일과 귀국일을 모두 여행 일수에 포함합니다. 귀국일은 출발일 뒤여야 합니다.

```json
{
  "countryCode": "JP",
  "city": "Tokyo",
  "departureDate": "2026-12-01",
  "returnDate": "2026-12-07",
  "travelers": 2,
  "mode": "SIMPLE",
  "purposes": ["leisure"],
  "activities": ["trekking"],
  "providedLodgingAmenities": [],
  "personalSituations": []
}
```

| 필드 | 규칙 |
|---|---|
| `countryCode` | 필수 ISO 3166-1 alpha-2 코드. `JP`, `VN`, `US`, `TH`, `TW`만 허용하며 미지원 코드는 서버가 거부 |
| `city` | 선택, 최대 100자. 비어 있으면 국가 카탈로그의 대표 도시를 사용. 도시명과 국가의 일치 검증은 지오코딩 연동 후 제공 |
| `departureDate`, `returnDate` | 필수 날짜, 귀국일은 출발일 뒤 |
| `travelers` | 필수 정수, 1명 이상. 공동용 준비물 수량 산정에는 사용하지 않음 |
| `mode` | `SIMPLE` 또는 `STANDARD` |
| `purposes`, `activities`, `providedLodgingAmenities`, `personalSituations` | 필수 배열(없으면 빈 배열), 영문·숫자·하이픈 토큰 |

성공 시 `200 OK`와 여행 정보 및 항목 목록을 반환합니다. 항목에는 `itemId`, 이름·별칭, 카테고리·범위, 계산 수량, 일치한 근거 태그, `recommendationType`(`BASIC` 또는 `CONDITIONAL`), 규칙의 기본 추천 문구가 포함됩니다. `requiredTags`가 비어 있으면 기본 항목으로 항상 후보에 포함되고, 하나 이상이면 입력 태그 중 하나와 일치해야 추가되는 조건부 항목입니다. `excludeTags`가 일치하면 기본/조건부와 관계없이 결과에서 제외됩니다. 프론트엔드는 `recommendationType`이 `CONDITIONAL`인 항목에만 접힌 추천 이유 펼치기를 표시합니다. 공동용 수량은 기존 규칙 엔진에 따라 1이며 인원수는 응답 메타데이터로만 전달됩니다.

잘못된 요청은 `400 Bad Request`와 아래 형식으로 반환합니다.

```json
{
  "code": "VALIDATION_ERROR",
  "message": "요청 값이 올바르지 않습니다.",
  "violations": [{ "field": "travelers", "message": "..." }]
}
```

형식은 유효하지만 지원 목록 밖인 `countryCode`는 `INVALID_TRIP` 코드와 함께 같은 `400` 상태로 거부합니다.

## `GET /api/v1/countries`

MVP 지원 국가를 표시 순서대로 반환합니다. 각 항목은 `countryCode`, 한국어 `name`, 도시 미입력 시 쓸 `representativeCity`를 제공합니다.

```json
[
  { "countryCode": "JP", "name": "일본", "representativeCity": "도쿄" },
  { "countryCode": "VN", "name": "베트남", "representativeCity": "하노이" },
  { "countryCode": "US", "name": "미국", "representativeCity": "워싱턴 D.C." },
  { "countryCode": "TH", "name": "태국", "representativeCity": "방콕" },
  { "countryCode": "TW", "name": "대만", "representativeCity": "타이베이" }
]
```

도시 이름을 국가와 대조하거나 오타를 판정하는 기능은 아직 없습니다. Open-Meteo 지오코딩을 연결할 때 해당 국가로 검색을 제한하고, 결과가 없거나 불일치하면 재입력을 요청해야 합니다.

간단 모드 요청에 숙소 제공 물품 또는 개인 상황 값이 전달되더라도 서버는 이를 무시합니다. 표준 모드에서만 해당 입력이 규칙 태그에 반영됩니다.
