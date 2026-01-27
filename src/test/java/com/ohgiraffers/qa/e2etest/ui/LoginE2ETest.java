package com.ohgiraffers.qa.e2etest.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

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
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    // TC 3. 로그인 성공
    @Test
    void login_success_redirects_to_board_list() {
        page.navigate(BASE_URL + "/user/login");

        page.locator("input[name='loginId']").fill("test123");
        page.locator("input[name='password']").fill("1234");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("로그인")).click();

        page.waitForURL("**/board/list*");
        assertThat(page).hasURL(Pattern.compile(".*/board/list.*"));
    }

    // TC 4. 로그인 실패(ID 없음)
    @Test
    void login_fail_when_id_not_exists_shows_login_fail_page() {
        page.navigate(BASE_URL + "/user/login");

        page.locator("input[name='loginId']").fill("not_exist_id_1234");
        page.locator("input[name='password']").fill("123456");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("로그인")).click();

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("로그인 실패")))
                .isVisible();
        assertThat(page.getByText("아이디 또는 비밀번호가 올바르지 않습니다."))
                .isVisible();
    }

    // TC 5. 로그인 실패(비밀번호 불일치)
    @Test
    void login_fail_when_password_is_wrong_shows_login_fail_page() {
        page.navigate(BASE_URL + "/user/login");

        page.locator("input[name='loginId']").fill("test123");
        page.locator("input[name='password']").fill("wrong_pw");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("로그인")).click();

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("로그인 실패")))
                .isVisible();
        assertThat(page.getByText("아이디 또는 비밀번호가 올바르지 않습니다."))
                .isVisible();
    }
}

