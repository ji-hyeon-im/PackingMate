# 체크리스트 규칙 도메인 테스트 기록

## 테스트 범위

`ChecklistItem` 필수값과 불변 컬렉션, 고정·일수 비례 수량 규칙, 최대 수량 상한 및 잘못된 입력을 확인한다.

## 조건별 결과

| 조건 | 기대 결과 | 실제 결과 | 발견한 문제 | 수정 사항 | 재시험 결과 |
|---|---|---|---|---|---|
| 3일 여행, `DailyCap(1)`, 최대 10 | 4개 | 사용자 로컬에서 테스트 명령 `BUILD SUCCESSFUL` | 실행 환경 간 Gradle native library/cache 접근 차이 | 코드 변경 없이 사용자 로컬 Gradle로 실행 | 성공 확인 |
| 8일 여행, `DailyCap(1)`, 최대 5 | 최대치인 5개 | 사용자 로컬에서 테스트 명령 `BUILD SUCCESSFUL` | 동일 | 코드 변경 없이 사용자 로컬 Gradle로 실행 | 성공 확인 |
| 5일 여행, `Rotation(2)` | 올림하여 3개 | 사용자 로컬에서 테스트 명령 `BUILD SUCCESSFUL` | 동일 | 코드 변경 없이 사용자 로컬 Gradle로 실행 | 성공 확인 |
| 4일 여행, `Consumable(3)` | 올림하여 2개 | 사용자 로컬에서 테스트 명령 `BUILD SUCCESSFUL` | 동일 | 코드 변경 없이 사용자 로컬 Gradle로 실행 | 성공 확인 |
| 준비물 최대 수량 0 또는 여행 일수 0 | 잘못된 입력 예외 | 사용자 로컬에서 테스트 명령 `BUILD SUCCESSFUL` | 동일 | 코드 변경 없이 사용자 로컬 Gradle로 실행 | 성공 확인 |

도메인 소스는 `javac --release 21` 컴파일에도 성공했다. 에이전트 환경에서는 Gradle 캐시 권한 및 native library 오류로 테스트를 시작하지 못했지만, 사용자가 로컬에서 해당 테스트 명령을 실행해 `BUILD SUCCESSFUL`을 확인했다.

실행 명령: `cd backend && ./gradlew test --tests com.packingmate.domain.ChecklistItemTest`

Gradle 실행 환경이 복구되면 이 테스트를 다시 실행하고 실제 assertion 결과 및 재시험 결과로 갱신한다. 실제 실행 전 결과를 추정해 기재하지 않는다.

## SQL 초기 데이터 검증

`backend/src/test/resources/sql/checklist_seed_assertions.sql`은 PostgreSQL에서 시드 항목 수(40~60개), 별칭·조건 태그 존재, 속옷 일수 비례 규칙, 상의 순환 착용 규칙, 개인용 소모품 및 공동용 수량 1 고정을 확인한다.

PostgreSQL 데이터베이스가 실행 중이고 `packingmate` 데이터베이스 및 사용자가 준비되어 있을 때 저장소 루트에서 실행한다.

```bash
cd backend
psql -h localhost -U packingmate -d packingmate -v ON_ERROR_STOP=1 \
  -f src/main/resources/schema.sql \
  -f src/main/resources/data.sql \
  -f src/test/resources/sql/checklist_seed_assertions.sql
```

기대 결과는 명령 종료 코드 0과 `체크리스트 시드 검증 성공` NOTICE이다. 실제 실행 결과, 발견한 문제 및 수정·재시험 결과는 실행 후 이 기록에 추가한다. 이번 파일 작업에서는 요청에 따라 테스트를 실행하지 않았다.

## 태그 기반 규칙 엔진

| 조건 | 기대 결과 | 실제 결과 | 발견한 문제 | 수정 사항 | 재시험 결과 |
|---|---|---|---|---|---|
| 기본 항목 및 필요 태그 여러 개 중 하나 일치 | 기본 항목과 조건 항목 포함 | 이번 구현에서 미실행 | 실행 전 미확인 | 미확인 | 미실행 |
| 제외 태그와 같은 태그가 존재 | 해당 항목 제외 | 이번 구현에서 미실행 | 실행 전 미확인 | 미확인 | 미실행 |
| 같은 itemId의 여러 조건이 일치 | 항목 1개로 병합, 근거 태그 합침 | 이번 구현에서 미실행 | 실행 전 미확인 | 미확인 | 미실행 |
| 공동용 항목과 개인용 일수 비례 항목 | 공동용 1개, 개인용은 계산 후 상한 적용 | 이번 구현에서 미실행 | 실행 전 미확인 | 미확인 | 미실행 |
| 기온 경계값·강수·기간·숙소·개인 상황 입력 | 지정 상황 태그 생성 | 이번 구현에서 미실행 | 실행 전 미확인 | 미확인 | 미실행 |

이번 구현에서는 요청에 따라 테스트를 실행하지 않았다.

```bash
cd backend
./gradlew test --tests com.packingmate.rule.TagGeneratorTest --tests com.packingmate.rule.RuleEngineTest
```

이 명령은 규칙 엔진의 순수 단위 테스트를 실행한다. Spring 컨텍스트와 DB 연결까지 확인하려면 PostgreSQL을 실행하고 `DB_PASSWORD` 환경 변수를 설정한 뒤 `./gradlew test`를 실행한다.
