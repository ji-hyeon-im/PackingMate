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
| `countryCode` | 필수 ISO 3166-1 alpha-2 두 글자 형식. 아직 공식 국가 코드 목록을 연결하지 않아 실제 할당·지원 여부는 검증하지 않음 |
| `city` | 선택, 최대 100자 |
| `departureDate`, `returnDate` | 필수 날짜, 귀국일은 출발일 뒤 |
| `travelers` | 필수 정수, 1명 이상. 공동용 준비물 수량 산정에는 사용하지 않음 |
| `mode` | `SIMPLE` 또는 `STANDARD` |
| `purposes`, `activities`, `providedLodgingAmenities`, `personalSituations` | 필수 배열(없으면 빈 배열), 영문·숫자·하이픈 토큰 |

성공 시 `200 OK`와 여행 정보 및 항목 목록을 반환합니다. 항목에는 `itemId`, 이름·별칭, 카테고리·범위, 계산 수량, 일치한 근거 태그, 규칙의 기본 추천 문구가 포함됩니다. 공동용 수량은 기존 규칙 엔진에 따라 1이며 인원수는 응답 메타데이터로만 전달됩니다.

잘못된 요청은 `400 Bad Request`와 아래 형식으로 반환합니다.

```json
{
  "code": "VALIDATION_ERROR",
  "message": "요청 값이 올바르지 않습니다.",
  "violations": [{ "field": "travelers", "message": "..." }]
}
```

현재 단계에서는 국가 목록 API, 지원 국가 여부 확인, 날씨 태그 및 표준 모드의 숙소·개인 상황 활용 차이는 구현하지 않았습니다. 국가 카탈로그와 날씨·전압 데이터 준비 후 지원 국가 조회와 국가 검증을 연결합니다.
