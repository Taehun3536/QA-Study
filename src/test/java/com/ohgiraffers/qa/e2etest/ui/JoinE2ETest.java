package com.ohgiraffers.qa.e2etest.ui;

import com.microsoft.playwright.*;
import com.ohgiraffers.qa.e2etest.ui.page.JoinPage;
import org.junit.jupiter.api.*;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("e2e")
public class JoinE2ETest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    // Page Object 추가
    JoinPage joinPage;

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
        // Page Object 초기화
        joinPage = new JoinPage(page);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    @DisplayName("TC 1: 회원가입 성공 시 로그인 페이지로 이동한다.")
    void join_success_redirects_to_login_page() {
        // Given & When
        joinPage.navigate();
        joinPage.join("user_" + System.currentTimeMillis(), "1234", "닉네임");

        // Then
        assertThat(page).hasURL(Pattern.compile(".*/user/login.*"));
    }

    @Test
    @DisplayName("TC 2: 중복 아이디로 가입 시도 시 가입 페이지에 잔류한다.")
    void join_fail_when_duplicate_id_stays_on_join_page() {
        String duplicateId = "dup_" + System.currentTimeMillis();

        // 1) 최초 가입
        joinPage.navigate();
        joinPage.join(duplicateId, "1234", "닉네임1");
        page.waitForURL("**/user/login*");

        // 2) 중복 아이디 재가입
        joinPage.navigate();
        joinPage.join(duplicateId, "1234", "닉네임2");

        // Then: 가입 페이지 URL 유지 검증
        assertThat(page).hasURL(Pattern.compile(".*/user/join.*"));
    }
}