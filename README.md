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
- PostgreSQL은 백엔드 DB 연동 단계에서 설정

## 실행

프론트엔드:

```bash
cd frontend
npm ci
npm run dev
```

백엔드:

```bash
cd backend
./gradlew bootRun
```

현재 백엔드는 Spring Boot 기본 골격입니다. PostgreSQL, 외부 API, Docker 실행 설정은 해당 기능을 구현하면서 추가합니다. `.env.example`은 변수 이름 안내용이며, 실제 비밀값은 로컬 `.env`에 두고 커밋하지 않습니다.

## 개발 원칙

- 프론트엔드 코드는 `frontend/`, 백엔드 코드는 `backend/`에 둡니다.
- 설계와 구현의 주요 변경 사항은 `docs/`에 기록합니다.
- 기능 브랜치에서는 구현과 관련 테스트를 함께 작성한 뒤 `main`에 병합합니다.
