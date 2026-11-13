# PackingMate

해외여행 정보를 바탕으로 준비물 체크리스트를 만드는 모노레포 프로젝트입니다.

## 저장소 구성

```text
PackingMate/
├── .env.example
├── frontend/   # React, TypeScript, Vite
├── backend/    # Java 21, Spring Boot, Gradle
└── docs/       # 설계 및 프로젝트 기록
```

## 개발 환경

- Node.js: Vite 버전과 호환되는 버전
- Java 21
- PostgreSQL 16 (백엔드 규칙 데이터 저장)

## 실행

프론트엔드:

```bash
cd frontend
npm ci
npm run dev
```

백엔드 (PostgreSQL 실행 후 `DB_PASSWORD` 환경 변수를 설정):

```bash
cd backend
./gradlew bootRun
```

백엔드는 PostgreSQL에서 `schema.sql`과 `data.sql`을 시작 시 적용합니다. 기본 연결은 `localhost:5432`, 데이터베이스·사용자 `packingmate`이며 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경 변수로 재설정할 수 있습니다. Gradle은 `.env`를 자동으로 읽지 않으므로 실행 셸에서 환경 변수를 설정해야 합니다. `.env.example`은 변수 이름 안내용이며, 실제 비밀값은 로컬 `.env`에 두고 커밋하지 않습니다. 외부 API와 Docker 실행 설정은 후속 작업입니다.

## 개발 원칙

- 프론트엔드 코드는 `frontend/`, 백엔드 코드는 `backend/`에 둡니다.
- 설계와 구현의 주요 변경 사항은 `docs/`에 기록합니다.
- 기능 브랜치에서는 구현과 관련 테스트를 함께 작성한 뒤 `main`에 병합합니다.
