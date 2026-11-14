# 2026-11-14 전체 검증 기록

## 자동화된 백엔드 검증

실행 명령은 저장소의 `backend` 디렉터리에서 다음 환경 변수와 함께 수행했다. 비밀번호는 기록하지 않는다.

```bash
DB_URL=jdbc:postgresql://localhost:5432/packingmate \
DB_USERNAME=packingmate \
DB_PASSWORD='<로컬 DB 비밀번호>' \
./gradlew test
```

결과: `BUILD SUCCESSFUL`. Gradle 테스트 XML 리포트(2026-11-14 11:04 KST)를 확인한 결과 **21개 테스트, 실패 0, 오류 0, 건너뜀 0**이다. `BackendApplicationTests.contextLoads`를 포함한 전체 테스트가 성공했다.

테스트 프로세스의 `DB_URL`은 `localhost:5432`였다. Compose 설정에서 PostgreSQL 컨테이너 포트는 호스트에 게시되지 않고(`5432/tcp`만 노출), 테스트 출력의 Hikari 로그에는 PostgreSQL 연결이 성공적으로 추가되었다. 따라서 이 Gradle 실행은 Docker Compose의 DB가 아니라 호스트의 로컬 PostgreSQL을 사용한 것으로 판단한다.

`application.yaml`은 SQL 초기화 모드를 `always`로 설정한다. 전체 컨텍스트 테스트가 성공했고 Hikari PostgreSQL 연결이 성립했으므로 애플리케이션 컨텍스트 시작 중 `schema.sql`·`data.sql` 초기화도 오류 없이 완료된 것으로 확인한다. 다만 별도 `psql` 시드 assertion 스크립트를 실행한 기록은 이번 결과에 포함되지 않는다.

## 프론트엔드 검증

`frontend`에서 `npm ci`, `npm run lint`, `npm run build`를 순서대로 실행했다.

| 항목 | 결과 |
|---|---|
| `npm ci` | 성공. 패키지 161개 설치, 162개 감사. moderate 취약점 9개 보고 |
| `npm run lint` | 성공. ESLint 오류 출력 없음 |
| `npm run build` | 성공. `tsc -b` 통과, Vite 8.3.8이 19개 모듈 변환 |

설치 중 `fsevents@2.3.3` 설치 스크립트가 allowScripts 승인 목록에 없다는 npm 경고도 출력됐다. 설치는 완료됐으며, 취약점 경고는 테스트 실패를 뜻하지 않지만 별도 `npm audit` 검토 항목으로 남긴다.

프론트엔드에는 별도의 단위·컴포넌트 테스트 스크립트가 없으므로 위 결과는 린트와 빌드 검증이다.

## 수동 UI·브라우저 검증

사용자 확인에 따라 이 시점에 계획된 UI·브라우저 시나리오를 모두 확인했다. 이전 기록에서 미검증으로 표시했던 추천 이유 표시, `localStorage` 복원 및 손상 데이터 처리, 실제 결과 화면 확인을 포함한다. 이 항목들은 자동화된 브라우저 테스트 결과가 아니라 수동 확인 결과다.

## Compose 실행 상태

`docker compose ps`에서 `packingmate-app-1`은 `Up`, `packingmate-db-1`은 `Up (healthy)`였고 앱 포트는 `127.0.0.1:8080`에 바인딩되어 있었다. 이는 확인 시점의 컨테이너 상태 기록이며, 그 자체로 브라우저 시나리오나 API 종단 간 테스트를 의미하지 않는다.
