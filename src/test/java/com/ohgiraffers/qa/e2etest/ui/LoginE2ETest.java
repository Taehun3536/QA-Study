package com.ohgiraffers.qa.e2etest.ui;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.ohgiraffers.qa.e2etest.ui.page.JoinPage;
import com.ohgiraffers.qa.e2etest.ui.page.LoginPage;
import org.junit.jupiter.api.*;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("e2e")
public class LoginE2ETest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    // 테스트 환경 설정 (중앙 관리)
    static final String BASE_URL = "http://localhost:8080";

    // Page Objects
    JoinPage joinPage;
    LoginPage loginPage;

    @BeforeAll
    static void beforeAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void afterAll() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();

        // [중요] 모든 Page Objects에 BASE_URL 주입
        joinPage = new JoinPage(page, BASE_URL);
        loginPage = new LoginPage(page, BASE_URL);

        page.setDefaultTimeout(10_000);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    /**
     * Helper: 테스트 시나리오 수행 전 유저를 생성합니다.
     * POM을 활용해 중복 코드를 제거하고 가독성을 높였습니다.
     */
    private String createTestUser() {
        String uid = "test_" + System.currentTimeMillis();
        joinPage.navigate();
        joinPage.join(uid, "1234", "테스터");
        page.waitForURL("**/user/login*");
        return uid;
    }

    @Test
    @DisplayName("TC 3: 유효한 계정으로 로그인 시 게시판 목록으로 이동한다.")
    void login_success_redirects_to_board_list() {
        // Given: 유저 생성
        String uid = createTestUser();

        // When: 로그인 시도
        loginPage.navigate();
        loginPage.login(uid, "1234");

        // Then: URL 및 헤더 검증
        page.waitForURL("**/board/list*");
        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("게시판 목록"))).isVisible();
    }

    @Test
    @DisplayName("TC 4: 잘못된 비밀번호 입력 시 로그인 실패 메시지를 표시한다.")
    void login_fail_when_password_is_wrong_shows_login_fail_page() {
        // Given: 유저 생성
        String uid = createTestUser();

        // When: 잘못된 정보로 로그인
        loginPage.navigate();
        loginPage.login(uid, "wrong_pw");

        // Then: 실패 페이지 헤더 및 안내 메시지 확인
        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("로그인 실패"))).isVisible();
        assertThat(page.getByText("아이디 또는 비밀번호가 올바르지 않습니다.")).isVisible();
    }
}