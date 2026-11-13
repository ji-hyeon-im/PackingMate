# PackingMate

해외여행 정보를 바탕으로 준비물 체크리스트를 만드는 모노레포 프로젝트입니다.

## 저장소 구성

```text
PackingMate/
├── .env.example
├── frontend/   # React, TypeScript, Vite
├── backend/    # Java 21, Spring Boot, Gradle
└── docs/       # 설계, API 계약, 테스트 기록
```

## 개발 환경

- Node.js: Vite와 호환되는 버전
- Java 21
- PostgreSQL 16

## 실행

PostgreSQL을 실행하고 `packingmate` 데이터베이스·사용자를 준비한 뒤, 백엔드 실행 셸에서 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경 변수를 설정합니다. 백엔드는 시작 시 `schema.sql`과 `data.sql`을 적용합니다. Gradle은 `.env` 파일을 자동으로 읽지 않습니다.

백엔드:

```bash
cd backend
./gradlew bootRun
```

프론트엔드(별도 터미널):

```bash
cd frontend
npm ci
npm run dev
```

Vite 개발 서버는 `/api` 요청을 `http://localhost:8080`의 백엔드로 전달합니다. 체크리스트 생성을 사용하려면 PostgreSQL을 준비하고 백엔드를 먼저 실행해야 합니다.

## 체크리스트 UI

입력 화면은 목적지, 여행 날짜와 인원, 여행 목적·활동을 단계별로 받습니다. 표준 모드에서는 숙소 제공 물품과 개인 상황도 입력할 수 있습니다. 결과는 개인용·공동용으로 나뉘며 항목 체크, 기존 항목 수량 조정, 사용자 항목 추가·삭제를 지원합니다. 입력·결과·체크 상태는 브라우저 `localStorage`에 저장됩니다.

현재 지원 국가 카탈로그가 준비되지 않아 목적지는 ISO 3166-1 alpha-2 국가 코드로 입력합니다. 서버는 코드 형식만 검증합니다. 예: `JP`.

## 환경 변수와 보안

기본 DB 연결은 `localhost:5432`, 데이터베이스·사용자 `packingmate`입니다. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`로 변경할 수 있습니다. `.env.example`은 변수 이름 안내용이며, 실제 비밀값은 로컬 `.env`에 두고 커밋하지 않습니다.

## 개발 원칙

- 프론트엔드는 `frontend/`, 백엔드는 `backend/`에 둡니다.
- 설계와 구현의 주요 결정, API 계약, 테스트 기록, 데이터 출처와 기준은 `docs/`에 남깁니다.
- 기능 브랜치에서 기능과 관련 테스트를 함께 작성합니다.
- 테스트는 담당 개발자가 직접 실행하고 결과를 기록합니다.

외부 날씨·Gemini 연동과 Docker 실행 설정은 후속 작업입니다.
