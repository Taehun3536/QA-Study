#[QA Test-Bed] 게시판 CRUD 및 비즈니스 로직 검증

1년 5개월간 물류 플랫폼 QA로서 800건 이상의 결함을 관리하며 쌓은 실무 감각을 바탕으로, 수동 테스트의 한계를 기술적으로 해결하기 위해 구축한 테스트 자동화 프로젝트입니다.
단순한 기능 동작 확인을 넘어, **사용자 상태 전이(State Transition)**에 따라 시스템이 API와 UI 양쪽 관점에서 입체적으로 반응하는지 검증하는 데 목적을 두고 있습니다.

🛠 Tech Stack
Language: Java 17

Framework: Spring Boot 3.x, Spring Data JPA

Testing: JUnit5, MockMvc, Playwright

CI/CD: GitHub Actions (MySQL & Browser 환경 자동화 구축)

🎯 주요 테스트 전략 및 설계 패턴
1. 비즈니스 로직 및 권한 검증 (API 통합 테스트)
CRUD 데이터 정합성: 게시글의 생명주기(생성→조회→수정→삭제) 과정에서 DB와 서버 간의 데이터 일치 여부를 MockMvc로 검증합니다.

보안 및 권한 로직: 로그인 여부, 작성자 권한(403 Forbidden), 존재하지 않는 리소스 접근(404 Not Found) 등 실제 서비스에서 발생할 수 있는 예외 상황을 집중적으로 체크합니다.

2. POM(Page Object Model) 기반의 UI 자동화 (Playwright)
유지보수 중심 설계: UI 요소(Locator)와 테스트 시나리오를 분리하는 POM 패턴을 적용했습니다. 이를 통해 UI 변경 시에도 스크립트 전체를 수정하지 않고 페이지 객체만 업데이트하여 관리 비용을 최소화했습니다.

사용자 여정 검증: 회원가입부터 게시글 관리까지의 전체 E2E 시나리오를 자동화하여, 크로스 브라우징 환경에서의 안정성을 확보했습니다.

3. 지속적 통합(CI) 환경 구축
GitHub Actions 파이프라인: 모든 코드 Push 시 가상 환경에서 MySQL 컨테이너를 구동하고 Playwright 브라우저 의존성을 설치하여 전체 테스트를 자동으로 수행하도록 워크플로우를 직접 설계했습니다.

신뢰성 확보: 상단의 빌드 배지를 통해 코드의 무결성을 실시간으로 증명합니다.

🏃 테스트 실행 방법
API 통합 테스트
Bash
./gradlew clean test
UI E2E 테스트 (Playwright)
Bash
./gradlew e2eTest
📈 성과 및 향후 목표
자동화 치환: 실무에서 반복적으로 수행하던 핵심 기능(Smoke Test)을 자동화 스크립트로 구현하여 회귀 테스트 효율을 높였습니다.

고도화: 현재 구축된 CI 환경을 바탕으로 테스트 실패 시 스크린샷 및 Trace 로그를 저장하는 리포팅 기능을 추가할 예정입니다.
