package com.ohgiraffers.qa.e2etest.ui;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.*;

@Tag("e2e")
public class JoinE2ETest {
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
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    // TC 1. 회원가입 성공
    @Test
    void join_success_redirects_to_login_page() {
        // Given
        page.navigate(BASE_URL + "/user/join");

        // When
        String uid = "test_" + System.currentTimeMillis(); // 중복 방지
        page.locator("input[name='loginId']").fill(uid);
        page.locator("input[name='password']").fill("1234");
        page.locator("input[name='nickname']").fill("닉네임");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("가입하기")).click();

        // Then
        assertThat(page).hasURL(BASE_URL + "/user/login");
    }

    // TC 2. 회원가입 실패(중복 ID)
    @Test
    void join_fail_when_duplicate_id_stays_on_join_page() {
        String duplicateId = "dup_" + System.currentTimeMillis();

        // 1) 1회 가입 성공으로 중복 상태 만들기
        page.navigate(BASE_URL + "/user/join");
        page.locator("input[name='loginId']").fill(duplicateId);
        page.locator("input[name='password']").fill("1234");
        page.locator("input[name='nickname']").fill("닉네임1");
        page.locator("button[type='submit']").click();
        page.waitForURL("**/user/login");

        // 2) 같은 ID로 재가입 시도 -> 실패 기대
        page.navigate(BASE_URL + "/user/join");
        page.locator("input[name='loginId']").fill(duplicateId);
        page.locator("input[name='password']").fill("1234");
        page.locator("input[name='nickname']").fill("닉네임2");
        page.locator("button[type='submit']").click();

        // 실패 검증(너희 앱 동작에 맞춰 택1)
        assertThat(page).hasURL(BASE_URL + "/user/join");
        // assertThat(page.getByText("이미 존재하는 아이디")).isVisible(); // 화면에 실제로 찍힐 때만
    }
}
