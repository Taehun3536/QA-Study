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

    // Page Objects 추가
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

        // Page Objects 초기화
        joinPage = new JoinPage(page);
        loginPage = new LoginPage(page);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    // helper: JoinPage 객체를 활용해 테스트 유저 생성
    private String createTestUser() {
        String uid = "test_" + System.currentTimeMillis();
        joinPage.navigate();
        joinPage.join(uid, "1234", "닉네임");
        page.waitForURL("**/user/login*");
        return uid;
    }

    @Test
    @DisplayName("TC 3: 유효한 계정으로 로그인 시 게시판 목록으로 이동한다.")
    void login_success_redirects_to_board_list() {
        String uid = createTestUser();

        loginPage.navigate();
        loginPage.login(uid, "1234");

        page.waitForURL("**/board/list*");
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("게시판 목록"))).isVisible();
    }

    @Test
    @DisplayName("TC 4: 잘못된 비밀번호 입력 시 로그인 실패 페이지를 표시한다.")
    void login_fail_when_password_is_wrong_shows_login_fail_page() {
        String uid = createTestUser();

        loginPage.navigate();
        loginPage.login(uid, "wrong_pw");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("로그인 실패"))).isVisible();
        assertThat(page.getByText("아이디 또는 비밀번호가 올바르지 않습니다.")).isVisible();
    }
}