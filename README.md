# PackingMate

해외여행 정보를 바탕으로 준비물 체크리스트를 만드는 모노레포 프로젝트입니다.

## 저장소 구성

```text
PackingMate/
├── .env.example
├── frontend/   # React, TypeScript, Vite
├── backend/    # Java 21, Spring Boot, Gradle
└── docs/       # 설계, API 계약, 출처와 결정 기록
```

## 개발 환경

- Node.js: Vite와 호환되는 버전
- Java 21
- PostgreSQL 16

## 실행

PostgreSQL을 실행하고 `packingmate` 데이터베이스·사용자를 준비한 뒤, 백엔드 실행 셸에서 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경 변수를 설정합니다. 백엔드는 시작 시 `schema.sql`과 `data.sql`을 적용합니다. 체크리스트 항목·별칭·태그와 국가별 전압·플러그 기준은 SQL 시드 전용이며, `data.sql`이 시작할 때마다 해당 테이블을 시드 내용으로 다시 채웁니다. 이 테이블에 직접 추가·수정한 내용은 다음 시작 때 보존되지 않습니다. Gradle은 `.env` 파일을 자동으로 읽지 않습니다.

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

## Docker 실행

Docker Desktop과 Compose v2가 필요합니다. 아래 명령은 저장소 루트에서 실행합니다.

1. 이전에 같은 이름의 환경 변수를 `export`했다면 지워 `.env` 값을 사용하게 합니다. 셸 변수는 `.env`보다 우선합니다 ([Docker 문서](https://docs.docker.com/compose/how-tos/environment-variables/envvars-precedence/)).

   ```bash
   unset DB_NAME DB_USERNAME DB_PASSWORD APP_PORT
   ```

2. 처음 실행할 때만 예시 파일을 복사합니다. `.env`가 이미 있으면 복사하지 말고 기존 파일을 편집하세요. `DB_PASSWORD`를 로컬용 비밀번호로 바꾸고 저장합니다.

   ```bash
   test -f .env || cp .env.example .env
   open -e .env
   ```

3. 설정을 비밀값 출력 없이 검증합니다. 성공하면 출력 없이 끝납니다.

   ```bash
   docker compose config --quiet
   ```

4. 앱 이미지를 빌드하고 앱·PostgreSQL을 시작합니다.

   ```bash
   docker compose up --build -d
   docker compose ps
   ```

   DB가 `healthy`이고 앱이 실행 중이면 [http://localhost:8080](http://localhost:8080)을 엽니다. 앱 로그는 `docker compose logs -f app`으로 확인할 수 있습니다. 로그 보기만 끝내려면 `Ctrl+C`를 누르세요. 컨테이너는 계속 실행됩니다.

앱은 로컬 호스트에만 공개되고 DB 포트는 호스트에 공개되지 않습니다. 로컬 Spring Boot가 이미 8080을 사용 중이면 종료하거나 `.env`에서 `APP_PORT=8081`로 바꾸세요.

**Docker 종료:** 종료할 때는 `docker compose down`을 실행합니다. PostgreSQL 볼륨은 보존됩니다. `docker compose down -v`는 DB 데이터를 삭제하므로 의도적으로 초기화할 때만 사용하세요. 앱 시작 때 기준 시드 테이블은 `data.sql` 내용으로 다시 채워집니다.

**로컬 PostgreSQL 종료:** `docker compose down`은 Compose로 실행한 PostgreSQL만 종료합니다. Homebrew로 설치한 로컬 PostgreSQL 16은 별도로 실행 중일 수 있습니다. 로컬 서버도 종료하려면 `brew services stop postgresql@16`을 실행하세요. `lsof -i :5432`로 로컬 PostgreSQL이 5432 포트를 사용 중인지 확인할 수 있습니다. Docker 실행 시 DB 포트는 호스트에 공개되지 않습니다.

## 현재 기능

- 일본·베트남·미국·태국·대만의 목적지와 도시, 여행 기간으로 규칙 기반 체크리스트를 생성합니다.
- 날씨는 Open-Meteo 예보를 사용하고, 필요한 경우 NASA POWER 월평균 자료로 대체합니다. 출처·상태·기준 기간은 결과에서 확인할 수 있습니다.
- 전압·주파수와 플러그 호환 상태를 안내합니다. 부분 호환이면 사용자가 어댑터를 선택하고, 호환 유형이 없으면 공동용 어댑터를 추천합니다.
- 결과를 개인용·공동용으로 나누고, 사용자가 체크 상태·수량·추가 항목을 관리할 수 있습니다. 입력, 결과, 체크 상태는 브라우저 `localStorage`에 저장됩니다.

표준 모드에서만 숙소 제공 물품과 개인 상황을 반영합니다. 서버는 여행 입력을 저장하지 않으며, 도시와 국가 코드는 날씨 지오코딩 제공자에게 전달하고 여행 날짜는 외부로 보내지 않습니다. 요청·응답의 세부 계약은 [API 문서](docs/api/checklist-api.md), 출처와 제품 규칙은 [출처·규칙 문서](docs/sources-and-rules.md)를 참고하세요.

## 환경 변수와 보안

기본 DB 연결은 `localhost:5432`, 데이터베이스·사용자 `packingmate`입니다. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`로 변경할 수 있습니다. `.env.example`은 변수 이름 안내용이며, 실제 비밀값은 로컬 `.env`에 두고 커밋하지 않습니다.

## 개발 원칙

- 프론트엔드는 `frontend/`, 백엔드는 `backend/`에 둡니다.
- 설계와 구현의 주요 결정, API 계약, 데이터 출처와 기준은 `docs/`에 남깁니다.
- 기능 브랜치에서 기능과 관련 테스트를 함께 작성합니다.
- 테스트는 담당 개발자가 직접 실행하고 결과를 기록합니다.

Gemini 연동은 MVP 이후 확장 기능입니다.

## 외부 자료와 배포 전 확인

날씨 자료는 [Open-Meteo](https://open-meteo.com/en/docs)와 [NASA POWER](https://power.larc.nasa.gov/docs/services/api/temporal/climatology/)를 사용합니다. Open-Meteo 무료 API의 이용 조건은 배포 전에 최신 약관을 확인해야 합니다. 전압·주파수·플러그 기준의 출처와 시드 적용 방식은 [출처·규칙 문서](docs/sources-and-rules.md)에 정리되어 있습니다.
