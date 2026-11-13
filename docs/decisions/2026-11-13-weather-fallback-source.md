# 날씨 월평균 fallback 출처 결정

- 결정일: 2026-11-13
- 상태: 적용

## 결정

Open-Meteo의 16일 예보 범위 밖 날짜 또는 예보 실패 시 NASA POWER Climatology API의 좌표별 월평균 자료를 사용한다. KMA 세계기후평년값을 fallback 데이터 원본으로 쓰지 않는다.

## 이유

KMA의 세계기후평년값은 관측 지점 기준 자료다. 확인 당시 지점 선택 목록에서 도쿄와 방콕은 확인했지만, PackingMate 지원 국가의 대표 도시인 하노이·워싱턴 D.C.·타이베이를 모두 확인할 수 없었다. 사용자가 임의로 입력하는 도시까지 지점 자료로 보장할 수 없다. 누락 도시를 다른 도시 자료로 대체하면 목적지와 다른 기후를 추천할 수 있다.

NASA POWER Climatology API는 위도·경도 좌표로 월평균 데이터를 제공해 지오코딩된 도시의 월평년을 요청할 수 있다. 2026-11-13 확인한 응답의 기후 변수는 MERRA-2 자료이며, 기간은 2001년 1월~2025년 12월로 표시됐다. 해당 기간은 응답에서 읽어 API 결과와 화면에 전달한다.

## 규칙과 표시

- Open-Meteo 예보와 NASA POWER fallback을 날짜 단위로 결합하고 실제 적용한 예보일·월평균일을 응답한다.
- 날씨 응답이 없으면 체크리스트는 규칙 엔진 결과로 반환하고 날씨 조건 태그는 생성하지 않는다.
- 한 날짜라도 예보의 일 강수량 또는 NASA 월평균 일 강수량이 1.0mm 이상이면 `weather:rain`을 생성한다. 이는 서비스 휴리스틱이다.
- 예보의 일 최고 UVI 또는 NASA 월평균 UVI가 3 이상이면 `weather:uv-protection`을 생성한다. WHO의 UVI 3 이상 보호 권고를 참고하며, 월평균 UVI가 개별 날짜의 최고 UVI를 뜻하지 않는 점을 화면·출처 문서에서 구분한다.
- 도시·국가 코드는 Open-Meteo 지오코딩으로 전달하고 반환 좌표는 예보 및 NASA POWER 조회에 사용한다. 여행 날짜는 서버에서만 처리한다. 요청 원문·좌표를 로그에 남기지 않는다.

## 출처

- [KMA 세계기후평년값](https://data.kma.go.kr/climate/average30Years/selectAverage30YearsWorldList.do?pgmNo=560)
- [Open-Meteo Forecast API](https://open-meteo.com/en/docs)
- [NASA POWER Climatology API](https://power.larc.nasa.gov/docs/services/api/temporal/climatology/)
- [NASA POWER 처리 방법](https://power.larc.nasa.gov/docs/methodology/data/processing/)
- [WHO 자외선 지침](https://www.who.int/news-room/fact-sheets/detail/ultraviolet-radiation)
- [Open-Meteo 이용 조건](https://open-meteo.com/en/terms)

현재 동작과 적용 상태는 [`sources-and-rules.md`](../sources-and-rules.md), 응답 필드와 상태값은 [`checklist-api.md`](../api/checklist-api.md)를 기준으로 확인한다.
