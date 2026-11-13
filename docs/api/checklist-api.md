# 체크리스트 생성 API 계약 (v1)

## `POST /api/v1/checklists`

JSON 요청을 검증하고 날씨 자료와 현재 DB의 준비물 규칙으로 추천 항목을 반환합니다. 날짜는 ISO-8601(`YYYY-MM-DD`)이며 출발일과 귀국일을 모두 여행 일수에 포함합니다. 귀국일은 출발일 뒤여야 합니다.

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
| `city` | 선택, 최대 100자. 비어 있으면 국가 카탈로그의 대표 도시를 사용. 날씨 지오코딩 결과가 없으면 도시명·국가 코드를 확인하라는 `400` 오류를 반환 |
| `departureDate`, `returnDate` | 필수 날짜, 귀국일은 출발일 뒤 |
| `travelers` | 필수 정수, 1명 이상. 공동용 준비물 수량 산정에는 사용하지 않음 |
| `mode` | `SIMPLE` 또는 `STANDARD` |
| `purposes`, `activities`, `providedLodgingAmenities`, `personalSituations` | 필수 배열(없으면 빈 배열), 영문·숫자·하이픈 토큰 |

성공 시 `200 OK`와 여행 정보, 날씨 요약, 전압·플러그 요약, 항목 목록을 반환합니다. 날씨 자료를 확인하지 못해도 체크리스트 생성을 중단하지 않으며 `weather.source`를 `UNAVAILABLE`로 반환하고 날씨 조건을 규칙에 반영하지 않습니다. 항목에는 `itemId`, 이름·별칭, 카테고리·범위, 계산 수량, 일치 태그, `recommendationType`(`BASIC` 또는 `CONDITIONAL`), 추천 문구가 포함됩니다. 기본·조건부 분류 및 추천 이유 동작은 이 API 응답 계약이며, 태그 매칭과 수량 산정 기준은 [출처·규칙 문서](../sources-and-rules.md)를 기준으로 합니다. 공동용 수량은 1이며 인원수는 응답 메타데이터로만 전달됩니다.

날씨 응답 필드는 다음과 같습니다.

```json
{
  "weather": {
    "source": "MIXED",
    "averageTemperatureCelsius": 18.2,
    "rainy": true,
    "uvProtectionRecommended": false,
    "forecastDays": 6,
    "monthlyAverageDays": 2,
    "climatologyPeriod": "25-year ... (January 2001 - December 2025)"
  }
}
```

`source`는 `OPEN_METEO`, `NASA_POWER`, `MIXED`, `PARTIAL_OPEN_METEO`, `UNAVAILABLE` 중 하나입니다. 예보일과 월평균 대체일이 섞이면 `MIXED`이며, 월평균 조회에 실패했지만 일부 예보만 있으면 `PARTIAL_OPEN_METEO`입니다. `climatologyPeriod`는 NASA POWER 응답에서 받은 기간이며 월평균을 사용하지 않으면 `null`입니다. 온도는 여행 일수로 가중한 평균입니다. `rainy`는 하루 강수량 1.0mm 이상 예보 또는 NASA POWER 월평균 일 강수량 1.0mm 이상을 뜻하는 제품 휴리스틱입니다. `uvProtectionRecommended`는 예보의 일 최고 UVI 또는 월평균 UVI가 3 이상일 때 `true`입니다.

전원 응답 필드는 다음과 같습니다.

```json
{
  "power": {
    "supplies": [
      { "voltage": 100, "frequency": 50, "note": "동일본(도쿄 포함)은 50Hz, 서일본(나고야·교토·오사카 등)은 60Hz." },
      { "voltage": 100, "frequency": 60, "note": "동일본(도쿄 포함)은 50Hz, 서일본(나고야·교토·오사카 등)은 60Hz." }
    ],
    "plugTypes": ["A"],
    "plugCompatibility": "INCOMPATIBLE",
    "voltageCheckRecommended": true,
    "frequencyCheckRecommended": true,
    "sources": [
      { "kind": "SUPPLY", "url": "https://www.sg.emb-japan.go.jp/itpr_en/japaninfo_electricity.html", "checkedOn": "2026-11-13" },
      { "kind": "PLUG", "url": "https://www.sg.emb-japan.go.jp/itpr_en/japaninfo_electricity.html", "checkedOn": "2026-11-13" }
    ]
  }
}
```
### 전원 응답
- `supplies`와 `plugTypes`는 국가별 전원·콘센트 기준이고, `sources`는 그 출처 URL과 확인일입니다.
- `plugCompatibility`는 한국 `C/F` 기준과의 호환 상태입니다. `INCOMPATIBLE`이면 `travel-adapter`를 자동 추가하고, `PARTIAL`이면 사용자가 선택합니다.
- 전압·주파수가 각각 `220V`·`60Hz`와 다르면 기기 정격 확인을 안내합니다. 이 비교로 준비물을 추가하지 않으며, 플러그 어댑터는 전압·주파수를 변환하지 않습니다.
- > 국가 기준은 숙소의 실제 규격을 보장하지 않습니다.

### 외부 전송과 저장
- 도시·국가 코드는 **Open-Meteo** 지오코딩에 전달하고, 조회된 좌표는 **Open-Meteo** 예보와 **NASA POWER** 조회에 사용합니다.
- 출발일·귀국일은 서버에서 처리하며 외부로 전송하지 않습니다. 서버는 여행 입력을 저장하지 않습니다.
- 외부 지오코딩 요청이 실패해도 체크리스트는 반환하고, 날씨 상태는 `UNAVAILABLE`로 표시합니다.

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

Open-Meteo 지오코딩은 요청의 `countryCode`로 결과 국가를 제한합니다. 입력 도시와 일치하는 지오코딩 결과가 없으면 `INVALID_TRIP` 오류로 재입력을 요청합니다. 외부 지오코딩 요청 자체가 실패하면 날씨를 `UNAVAILABLE`로 표시하고 체크리스트 생성을 계속합니다.

간단 모드 요청에 숙소 제공 물품 또는 개인 상황 값이 전달되더라도 서버는 이를 무시합니다. 표준 모드에서만 해당 입력이 규칙 태그에 반영됩니다.
