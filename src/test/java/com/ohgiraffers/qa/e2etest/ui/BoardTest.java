package com.ohgiraffers.qa.e2etest.ui;

import com.microsoft.playwright.*;
import com.ohgiraffers.qa.e2etest.ui.base.DbCleaner;
import com.ohgiraffers.qa.e2etest.ui.page.BoardPage;
import com.ohgiraffers.qa.e2etest.ui.page.JoinPage;
import com.ohgiraffers.qa.e2etest.ui.page.LoginPage;
import org.junit.jupiter.api.*;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("e2e")
public class BoardTest {
    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;

    // Page Objects
    JoinPage joinPage;
    LoginPage loginPage;
    BoardPage boardPage;

    // URL 패턴 정의
    static final Pattern BOARD_LIST_URL = Pattern.compile(".*/board/list(?:;jsessionid=.*)?$");
    static final Pattern LOGIN_URL = Pattern.compile(".*/user/login(?:;jsessionid=.*)?$");

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
        boardPage = new BoardPage(page);
    }

    @AfterEach
    void tearDown() {
        DbCleaner.deleteTestBoardsByPrefix();
        context.close();
    }

    // 공통 로그인 헬퍼
    private void joinAndLogin() {
        String uid = "test_" + System.currentTimeMillis();
        joinPage.navigate();
        joinPage.join(uid, "1234", "닉_" + uid);
        loginPage.login(uid, "1234");
    }

    @Test
    @DisplayName("TC 08: 게시글 작성 성공 시 목록에 표시되어야 한다.")
    void tc08_board_create_success_logged_in() {
        joinAndLogin();
        String title = "post_" + System.currentTimeMillis();
        boardPage.createPost(title, "테스트 내용");

        assertThat(page).hasURL(BOARD_LIST_URL);
        assertThat(page.getByText(title)).isVisible();
    }

    @Test
    @DisplayName("TC 13: 상세 페이지에서 게시글을 삭제할 수 있어야 한다.")
    void tc13_board_delete_success_logged_in() {
        joinAndLogin();
        String title = "post_" + System.currentTimeMillis();
        boardPage.createPost(title, "내용");
        boardPage.openDetail(title);
        boardPage.deletePostWithConfirm();

        assertThat(page).hasURL(BOARD_LIST_URL);
        assertThat(page.getByText(title)).hasCount(0);
    }

    @Test
    @DisplayName("TC 17: 비로그인 상태로 수정 시도 시 로그인으로 리다이렉트된다.")
    void tc17_board_edit_redirects_to_login_when_not_logged_in() {
        joinAndLogin();
        String title = "post_" + System.currentTimeMillis();
        boardPage.createPost(title, "내용");

        // 컨텍스트 전환 (비로그인 상태 재현)
        context.clearCookies();
        boardPage.navigateToList();
        boardPage.openDetail(title);
        page.getByRole(com.microsoft.playwright.options.AriaRole.LINK, new Page.GetByRoleOptions().setName("수정")).click();

        assertThat(page).hasURL(LOGIN_URL);
    }
}