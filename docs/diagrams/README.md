# PackingMate 다이어그램

이 폴더에는 「PackingMate 개발 설계 문서」 §4 프로그램 설계 구현도의 PlantUML 다이어그램을 개별 파일로 관리합니다.

| 파일 | 설명 |
|---|---|
| `01-system-component.puml` | 브라우저 UI, Spring Boot 서버, PostgreSQL, 외부 API 구성 |
| `02-checklist-activity.puml` | 입력 검증부터 Open-Meteo 날씨 조회, NASA POWER fallback, 체크리스트 반환까지의 흐름 |
| `03-docker-deployment.puml` | 앱·PostgreSQL 컨테이너, named volume과 외부 API 연결을 나타낸 현재 Docker 구성 |
| `04-aws-deployment.puml` | ALB, ECS Fargate, RDS, NAT Gateway와 외부 API 연결을 나타낸 AWS 배포 제안 |
| `05-checklist-sequence.puml` | 외부 API 호출과 NASA POWER fallback을 구분한 체크리스트 요청 흐름 |

Open-Meteo와 NASA POWER는 PackingMate 서버가 HTTPS로 호출하는 서로 다른 외부 API입니다. 
`.puml` 파일은 PlantUML 소스입니다. GitHub는 이 파일을 그림으로 자동 렌더링하지 않으므로, 다이어그램 이미지를 저장소에서 바로 보이게 하려면 PlantUML로 SVG 또는 PNG를 생성해 Markdown에서 삽입해야 합니다.
