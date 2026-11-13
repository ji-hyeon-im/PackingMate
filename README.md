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

PostgreSQL을 실행하고 `packingmate` 데이터베이스·사용자를 준비한 뒤, 백엔드 실행 셸에서 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경 변수를 설정합니다. 백엔드는 시작 시 `schema.sql`과 `data.sql`을 적용합니다. 체크리스트 항목·별칭·태그 테이블은 SQL 시드 전용이며, `data.sql`이 시작할 때마다 해당 테이블 전체를 시드 내용으로 다시 채웁니다. 이 테이블에 직접 추가·수정한 내용은 다음 시작 때 보존되지 않습니다. Gradle은 `.env` 파일을 자동으로 읽지 않습니다.

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

입력 화면은 목적지, 여행 날짜와 인원, 여행 목적·활동을 단계별로 받습니다. 표준 모드에서는 숙소 제공 물품과 개인 상황도 입력할 수 있습니다. 결과는 개인용·공동용으로 나뉘며, 조건부 추천 항목의 이유는 항목별 펼치기 컨트롤로 볼 수 있습니다. 항목 체크, 기존 항목 수량 조정, 사용자 항목 추가·삭제를 지원합니다. 입력·결과·체크 상태는 브라우저 `localStorage`에 저장됩니다.

목적지는 지원 국가 다섯 곳(일본, 베트남, 미국, 태국, 대만) 중 드롭다운으로 선택합니다. 화면은 한국어 국가명을 표시하고 API에는 ISO 3166-1 alpha-2 코드를 보냅니다. 도시는 직접 입력할 수 있고 비우면 카탈로그의 대표 도시를 사용합니다. 도시의 국가 일치·오타 검증은 지오코딩 연동 전까지 제공하지 않습니다.

## 환경 변수와 보안

기본 DB 연결은 `localhost:5432`, 데이터베이스·사용자 `packingmate`입니다. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`로 변경할 수 있습니다. `.env.example`은 변수 이름 안내용이며, 실제 비밀값은 로컬 `.env`에 두고 커밋하지 않습니다.

## 개발 원칙

- 프론트엔드는 `frontend/`, 백엔드는 `backend/`에 둡니다.
- 설계와 구현의 주요 결정, API 계약, 테스트 기록, 데이터 출처와 기준은 `docs/`에 남깁니다.
- 기능 브랜치에서 기능과 관련 테스트를 함께 작성합니다.
- 테스트는 담당 개발자가 직접 실행하고 결과를 기록합니다.

외부 날씨·Gemini 연동과 Docker 실행 설정은 후속 작업입니다.
