# 🛡️ QA Test-Bed: 게시판 CRUD 및 비즈니스 로직 검증

[![Java CI with MySQL and Playwright](https://github.com/Taehun3536/QA-Study/actions/workflows/test.yml/badge.svg)](https://github.com/Taehun3536/QA-Study/actions/workflows/test.yml)

> **"수동 테스트의 경험을 기술적 신뢰로 치환하다"**
> 1년 5개월간 물류 플랫폼 QA로서 800건 이상의 결함을 관리하며 쌓은 실무 감각을 바탕으로, 테스트 자동화를 통해 품질 관리의 효율성을 극대화한 프로젝트입니다.

---

## 🛠 Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x, Spring Data JPA |
| **Testing** | JUnit5, MockMvc (API), Playwright (UI) |
| **CI/CD** | GitHub Actions (MySQL & Browser 환경 구축) |
| **Database** | MySQL (Local/CI), H2 (Test Profile) |

---

## 🎯 주요 테스트 전략 및 설계 패턴

### 1. 비즈니스 로직 및 권한 검증 (API 통합 테스트)
* **CRUD 데이터 정합성**: 게시글의 생명주기(생성→조회→수정→삭제) 과정에서 DB와 서버 간의 데이터 일치 여부를 **MockMvc**로 검증합니다.
* **보안 및 권한 로직**: 
    * 로그인 여부에 따른 접근 제어
    * 작성자 외 수정/삭제 권한 제한 (**403 Forbidden**)
    * 존재하지 않는 리소스 접근 (**404 Not Found**) 등 실제 서비스의 예외 상황을 집중 관리합니다.

### 2. POM(Page Object Model) 기반의 UI 자동화 (Playwright)
* **유지보수 중심 설계**: UI 요소(Locator)와 테스트 시나리오를 분리하는 **POM 패턴**을 적용했습니다. UI가 변경되어도 페이지 객체만 업데이트하여 테스트 스크립트 유지보수 비용을 최소화합니다.
* **사용자 여정 검증**: 회원가입부터 로그인, 게시글 CRUD까지의 전체 **E2E 시나리오**를 자동화하여 크로스 브라우징 환경에서의 서비스 안정성을 확보했습니다.

### 3. 지속적 통합(CI) 환경 구축
* **GitHub Actions 파이프라인**: 
    * 코드 Push 시 가상 환경 내 **MySQL 컨테이너** 자동 구동
    * Playwright 브라우저 및 OS 의존성 설치
    * `bootRun`을 통한 서버 구동 및 테스트 자동 수행 (Health Check 포함)
* **신뢰성 확보**: 상단의 **Build Badge**를 통해 현재 코드의 무결성을 실시간으로 증명합니다.

---

## 🏃 테스트 실행 방법

### 🧪 API 통합 테스트
```bash
./gradlew clean test
