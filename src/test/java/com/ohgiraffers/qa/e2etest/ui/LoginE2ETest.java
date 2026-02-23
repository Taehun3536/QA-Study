package com.ohgiraffers.qa.e2etest.ui;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("e2e")
public class LoginE2ETest {
    static Playwright playwright;
    static Browser browser;

    BrowserContext context;
    Page page;

    static final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    static void beforeAll() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
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
        page.setDefaultTimeout(10_000);
        page.setDefaultNavigationTimeout(30_000);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    // ===== helper: 테스트용 유저 생성 =====
    private String createTestUser() {
        String uid = "test_" + System.currentTimeMillis();

        page.navigate(BASE_URL + "/user/join");
        page.locator("input[name='loginId']").fill(uid);
        page.locator("input[name='password']").fill("1234");
        page.locator("input[name='nickname']").fill("닉네임");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("가입하기")).click();

        // 가입 후 로그인 페이지로 이동하는지(환경에 따라 wait 필요)
        page.waitForURL("**/user/login*");
        return uid;
    }

    // TC 3. 로그인 성공
    @Test
    void login_success_redirects_to_board_list() {
        String uid = createTestUser(); // Given: 유저 존재

        page.navigate(BASE_URL + "/user/login");
        page.locator("input[name='loginId']").fill(uid);
        page.locator("input[name='password']").fill("1234");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("로그인")).click();

        // URL 이동이 먼저 안정적으로 되는지 기다렸다가,
        page.waitForURL("**/board/list*");

        // Then: 게시판 목록 헤딩 확인
        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("게시판 목록"))).isVisible();
    }

    // TC 4. 로그인 실패(ID 없음) - 유저 만들면 안 됨
    @Test
    void login_fail_when_id_not_exists_shows_login_fail_page() {
        page.navigate(BASE_URL + "/user/login");

        page.locator("input[name='loginId']").fill("not_exist_id_1234");
        page.locator("input[name='password']").fill("123456");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("로그인")).click();

        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("로그인 실패"))).isVisible();
        assertThat(page.getByText("아이디 또는 비밀번호가 올바르지 않습니다.")).isVisible();
    }

    // TC 5. 로그인 실패(비밀번호 불일치)
    @Test
    void login_fail_when_password_is_wrong_shows_login_fail_page() {
        String uid = createTestUser(); // Given: 유저 존재

        page.navigate(BASE_URL + "/user/login");
        page.locator("input[name='loginId']").fill(uid);
        page.locator("input[name='password']").fill("wrong_pw");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("로그인")).click();

        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("로그인 실패"))).isVisible();
        assertThat(page.getByText("아이디 또는 비밀번호가 올바르지 않습니다.")).isVisible();
    }
}
