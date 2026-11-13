# 체크리스트 규칙 테스트 및 개선 기록

아래 결과는 각 항목에 적힌 당시 코드와 명령에 대한 기록입니다. 현재 브랜치 전체의 빌드·테스트 상태를 뜻하지 않으며, 후속 구현의 검증은 해당 작업의 별도 기록이 필요합니다.

## 1. 준비물 규칙 도메인 모델

대상 커밋: `feat: 준비물 규칙 도메인 모델 추가`

| 확인 조건 | 기대 결과 | 실제 결과 | 발견한 문제·수정 | 재시험 결과 |
|---|---|---|---|---|
| `DailyCap(1)`, 3일, 상한 10 | 4개 계산 | 통과 | 문제 없음, 수정 없음 | `BUILD SUCCESSFUL` |
| `DailyCap(1)`, 8일, 상한 5 | 상한 5 적용 | 통과 | 문제 없음, 수정 없음 | `BUILD SUCCESSFUL` |
| `Rotation(2)`, 5일 | 올림하여 3개 | 통과 | 문제 없음, 수정 없음 | `BUILD SUCCESSFUL` |
| `Consumable(3)`, 4일 | 올림하여 2개 | 통과 | 문제 없음, 수정 없음 | `BUILD SUCCESSFUL` |
| 잘못된 여행 일수·최대 수량·수량 규칙 값 | 입력 예외 발생 | 통과 | 문제 없음, 수정 없음 | `BUILD SUCCESSFUL` |
| 입력 태그 집합 변경 후 모델 확인 | 생성 시점의 값 유지 | 통과 | 문제 없음, 수정 없음 | `BUILD SUCCESSFUL` |

실행 명령:

```bash
cd /Users/robin/클라우드프로그래밍/PackingMate/backend
./gradlew test --tests com.packingmate.domain.ChecklistItemTest
```

## 2. SQL 스키마 및 초기 준비물 데이터

대상 커밋: `feat: 준비물 규칙 데이터와 초기 항목 추가`

PostgreSQL 16 서비스가 실행 중인지 `pg_isready -h localhost -p 5432`로 확인하고, `packingmate` 사용자와 데이터베이스를 준비한 뒤 검증했다.

| 확인 조건 | 기대 결과 | 실제 결과 | 발견한 문제·수정 | 재시험 결과 |
|---|---|---|---|---|
| 테이블·인덱스 생성 | 스키마 적용 성공 | 테이블 4개, 인덱스 3개 생성 | 최초에는 서버 접속 거부. PostgreSQL 서비스를 시작하고 포트 응답을 확인함 | SQL 전체 검증 통과 |
| 기본 준비물 개수 | 40~60개 | 52개 | 문제 없음, 수정 없음 | 통과 |
| 별칭·필요 태그 시드 | 10개 이상 | 별칭 18개, 필요 태그 15개 | 문제 없음, 수정 없음 | 통과 |
| 수량 규칙·개인/공동 범위 | 속옷 `DAILY_CAP`, 상의 `ROTATION`, 개인 소모품 및 공동용 수량 1 | 검증 SQL 통과 | 문제 없음, 수정 없음 | `체크리스트 시드 검증 성공` NOTICE |

실행 명령:

```bash
cd /Users/robin/클라우드프로그래밍/PackingMate/backend
psql -h localhost -U packingmate -d packingmate -v ON_ERROR_STOP=1 \
  -f src/main/resources/schema.sql \
  -f src/main/resources/data.sql \
  -f src/test/resources/sql/checklist_seed_assertions.sql
```

Docker에서 PostgreSQL이 같은 `5432` 포트를 사용할 때는 로컬 PostgreSQL과 포트가 충돌하지 않도록 필요 시 `brew services stop postgresql@16`으로 로컬 서비스를 중지한다.

## 3. 태그 기반 체크리스트 규칙 엔진

대상 커밋: `feat: 태그 기반 체크리스트 규칙 엔진 구현`

| 확인 조건 | 기대 결과 | 실제 결과 | 발견한 문제·수정 | 재시험 결과 |
|---|---|---|---|---|
| 필요 태그가 여러 개이고 그중 하나가 입력됨 | 항목 포함 (OR 매칭) | 통과 | 문제 없음, 수정 없음 | `BUILD SUCCESSFUL` |
| 제외 태그가 입력 태그와 일치 | 항목 제외 | 통과 | 문제 없음, 수정 없음 | `BUILD SUCCESSFUL` |
| 같은 `itemId`에 여러 조건이 일치 | 한 항목으로 병합, 근거 태그 결합 | 통과 | 문제 없음, 수정 없음 | `BUILD SUCCESSFUL` |
| 개인용·공동용 수량 계산 | 개인용 규칙·상한 적용, 공동용 1개 | 통과 | 문제 없음, 수정 없음 | `BUILD SUCCESSFUL` |
| 기간·기온 경계·강수·숙소·개인 상황 | 해당 상황 태그 생성 | 통과 | 문제 없음, 수정 없음 | `BUILD SUCCESSFUL` |

실행 명령:

```bash
cd /Users/robin/클라우드프로그래밍/PackingMate/backend
./gradlew test --tests com.packingmate.rule.TagGeneratorTest --tests com.packingmate.rule.RuleEngineTest
```

위 결과는 사용자가 보고한 지정 단위 테스트와 SQL 검증 실행 결과를 기록한 것이다. 전체 `./gradlew test` 또는 Spring 컨텍스트를 포함한 통합 테스트 결과는 별도로 확인하지 않았다.
