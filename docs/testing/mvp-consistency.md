# 2026-11-13 MVP 모드 경계·저장·추천 이유 및 시드 검증 기록

검증일: 2026-11-13

이 문서는 해당 날짜에 수행한 검증의 이력이며 현재 코드 전체의 상태표가 아닙니다. 이후 날씨 지오코딩 등 구현 변경은 [`sources-and-rules.md`](../sources-and-rules.md)와 [`checklist-api.md`](../api/checklist-api.md)를 확인하세요. 아래 결과는 당시 실행 범위 그대로 보존합니다.

## 수행한 검증

| 확인 항목 | 기대 결과 | 실제 결과 | 제한/비고 |
|---|---|---|---|
| 백엔드 모드·국가·대표 도시 테스트 | SIMPLE은 숙소/개인 상황을 무시, STANDARD는 적용, 지원 국가만 허용, 도시 생략 시 대표 도시 설정 | 통과. 서비스 테스트 6개 | `./gradlew test --tests com.packingmate.service.ChecklistServiceTest --tests com.packingmate.controller.ChecklistControllerTest --tests com.packingmate.controller.CountryControllerTest` |
| 백엔드 API 컨트롤러 | 체크리스트 계약과 지원 국가 API의 HTTP 응답 확인 | 통과. 체크리스트/국가 컨트롤러 테스트 4개 | 같은 Gradle 명령으로 총 10개 테스트 성공 |
| 프론트엔드 프로덕션 빌드 | TypeScript 검사 및 Vite 빌드 통과 | 통과. Vite 19개 모듈 빌드 완료 |  |
| 프론트엔드 린트 | 오류·경고 없이 완료 | 통과 |  |
| 브라우저 입력 흐름 | 5개 국가 표시·선택, 대표 도시 placeholder, 단계 및 모드 전환 | 국가 드롭다운 5개와 대만 선택, 미입력 도시 기본값 타이베이 표시를 확인. 표준/간단 모드 전환도 확인 | 임시 실행은 SQL 초기화 없이 진행; 체크리스트 생성은 DB 미연결로 확인하지 못함 |
| 추천 이유 펼치기 | 조건부 항목에 기본으로 접힌 이유 컨트롤이 있고 펼치면 이유가 표시됨 | 미검증 | 결과 화면에 도달하지 못해 브라우저 확인 불가. 빌드는 통과했으나 실제 표시 검증은 남음 |
| localStorage 손상 데이터 복구 | 잘못된 내부 구조를 로드하지 않고 안전하게 초기 상태 사용 | 브라우저 시나리오 미검증 | 정적 검증 로직 구현, 기존 체크 상태 복원/손상값 동작을 브라우저에서 확인하지 않음 |
| 시드 재실행 및 동기화 | 반복 실행 후 현재 시드와 항목/관계 행이 정확히 일치 | 미검증 | localhost PostgreSQL 없음. 임시 PostgreSQL 초기화도 OS 공유 메모리 권한 제한으로 실패하여 SQL을 실행하지 못함 |
| 결과 API의 BASIC/CONDITIONAL 표시와 이유 펼치기 | 기본 항목은 이유 숨김, 조건부 항목에서 펼치기 | 미검증 | DB 기반 결과 화면을 띄우지 못해 브라우저 확인은 남음. API 응답 분류는 단위/컨트롤러 테스트 통과 |
| 도시 오타·국가 불일치 재입력 | 잘못된 도시 검색을 거부하고 재입력 요청 | 당시 미구현 | 이 검증 시점에는 도시 지오코딩이 연결되지 않았음. 이후 날씨 연동 상태는 현재 API 문서에서 확인 |

## 실행 명령

```bash
cd frontend
npm run build
npm run lint

cd ../backend
./gradlew test --tests com.packingmate.service.ChecklistServiceTest \
  --tests com.packingmate.controller.ChecklistControllerTest \
  --tests com.packingmate.controller.CountryControllerTest
```

백엔드 전체 테스트와 데이터베이스 통합 검증은 이번 기록에 포함하지 않는다. 실행하지 않은 검증 결과를 성공으로 간주하지 않는다.

이후 전체 백엔드 테스트와 UI·브라우저 시나리오 확인 결과는 [`2026-11-14 전체 검증 기록`](2026-11-14-full-verification.md)을 참고한다.
