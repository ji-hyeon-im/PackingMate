# 문서 안내

저장소와 서비스 이름은 PackingMate입니다. 설계 자료에 Packify가 남아 있더라도 현재 구현의 이름으로 읽습니다.

## 문서 책임

- [README](../README.md): 개발 환경, 실행 방법, 구현된 기능의 짧은 소개
- [`api/checklist-api.md`](api/checklist-api.md): 프론트엔드와 백엔드가 공유하는 요청·응답 계약
- [`sources-and-rules.md`](sources-and-rules.md): 현재 코드·시드에서 사용하는 출처, 제품 규칙, 자체 가정의 기준점
- [`decisions/`](decisions/): 지원 국가, 날씨 fallback, 시드 소유권, 전압·플러그에 관한 선택 이유와 영향
- [`testing/checklist-rules.md`](testing/checklist-rules.md): 초기 규칙 도메인·시드·엔진 검증 기록
- [`testing/checklist-ui.md`](testing/checklist-ui.md): 초기 UI 빌드·린트 기록
- [`testing/mvp-consistency.md`](testing/mvp-consistency.md): 모드·저장·브라우저 흐름의 후속 검증 기록
- [`testing/2026-11-14-full-verification.md`](testing/2026-11-14-full-verification.md): 전체 백엔드 테스트, 프론트엔드 검사, 브라우저 시나리오 및 Compose 상태 확인

결정 문서는 선택 이유와 당시 한계를 보존합니다. 중복되는 세부 동작은 API 계약이나 출처·규칙 문서의 기준을 따릅니다.

## 읽을 때 유의할 점

결정 문서와 테스트 기록은 작성 당시의 이력입니다. 현재 구현 상태는 코드와 출처·규칙 문서, API 계약을 대조해 확인합니다. 검증 기록에는 실행하지 않은 항목도 남을 수 있으며, 이를 성공으로 해석하지 않습니다.
