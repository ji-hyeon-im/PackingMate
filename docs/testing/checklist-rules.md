# 체크리스트 규칙 도메인 테스트 기록

## 테스트 범위

`ChecklistItem` 필수값과 불변 컬렉션, 고정·일수 비례 수량 규칙, 최대 수량 상한 및 잘못된 입력을 확인한다.

## 조건별 결과

| 조건 | 기대 결과 | 실제 결과 | 발견한 문제 | 수정 사항 | 재시험 결과 |
|---|---|---|---|---|---|
| 3일 여행, `DailyCap(1)`, 최대 10 | 4개 | JUnit 미실행: Gradle 시작 실패 | 설치된 Gradle이 macOS aarch64용 `libnative-platform.dylib`을 불러오지 못함 | 현재 환경에서 미수정 | 미실행 |
| 8일 여행, `DailyCap(1)`, 최대 5 | 최대치인 5개 | JUnit 미실행: Gradle 시작 실패 | 위와 같음 | 현재 환경에서 미수정 | 미실행 |
| 5일 여행, `Rotation(2)` | 올림하여 3개 | JUnit 미실행: Gradle 시작 실패 | 위와 같음 | 현재 환경에서 미수정 | 미실행 |
| 4일 여행, `Consumable(3)` | 올림하여 2개 | JUnit 미실행: Gradle 시작 실패 | 위와 같음 | 현재 환경에서 미수정 | 미실행 |
| 준비물 최대 수량 0 또는 여행 일수 0 | 잘못된 입력 예외 | JUnit 미실행: Gradle 시작 실패 | 위와 같음 | 현재 환경에서 미수정 | 미실행 |

도메인 소스는 별도로 `javac --release 21` 컴파일에 성공했다. 이는 JUnit 테스트 실행을 대체하지 않는다. Wrapper 실행도 기본 캐시 잠금 파일 권한 오류로 막혔고, 임시 캐시에서 배포본을 받으려 한 시도는 네트워크/DNS 제한으로 실패했다. 기존 Gradle 실행 파일을 직접 사용한 시도는 위의 native library 오류로 시작하지 못했다.

실행 명령: `cd backend && ./gradlew test --tests com.packingmate.domain.ChecklistItemTest`

Gradle 실행 환경이 복구되면 이 테스트를 다시 실행하고 실제 assertion 결과 및 재시험 결과로 갱신한다. 실제 실행 전 결과를 추정해 기재하지 않는다.
