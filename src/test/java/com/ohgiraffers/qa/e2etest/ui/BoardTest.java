package com.ohgiraffers.qa.e2etest.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.ohgiraffers.qa.e2etest.ui.base.DbCleaner;
import org.junit.jupiter.api.*;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("e2e")
public class BoardTest  {

    static Playwright playwright;
    static Browser browser;

    BrowserContext context;
    Page page;

    static final String BASE_URL = "http://localhost:8080";

    // URL에 ;jsessionid=... 가 붙는 환경 대응
    static final Pattern BOARD_LIST_URL = Pattern.compile(".*/board/list(?:;jsessionid=.*)?$");
    static final Pattern BOARD_DETAIL_URL = Pattern.compile(".*/board/detail/\\d+(?:;jsessionid=.*)?$");
    static final Pattern LOGIN_URL = Pattern.compile(".*/user/login(?:;jsessionid=.*)?$");
    static final Pattern BOARD_WRITE_URL = Pattern.compile(".*/board/write(?:;jsessionid=.*)?$");

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
        DbCleaner.deleteTestBoardsByPrefix();
        context.close();
    }


    // ---------- helpers ----------
    void joinAndLogin() {
        // 1) 회원가입
        page.navigate(BASE_URL + "/user/join");

        String uid = "test_" + System.currentTimeMillis();
        String pw = "1234";

        page.locator("input[name='loginId']").fill(uid);
        page.locator("input[name='password']").fill(pw);
        page.locator("input[name='nickname']").fill("닉_" + uid);

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("가입하기")
        ).click();

        Locator loginIdInput = page.locator("input[name='loginId']");
        assertThat(loginIdInput).isVisible();

        // 2) 로그인
        loginIdInput.fill(uid);
        page.locator("input[name='password']").fill(pw);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("로그인")).click();

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("게시판 목록"))).isVisible();
    }


    void createPost(String title, String content) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("게시글 작성하기")).click();
        assertThat(page).hasURL(BOARD_WRITE_URL);

        page.locator("input[name='title']").fill(title);
        page.locator("textarea[name='content']").fill(content);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("등록")).click();

        assertThat(page).hasURL(BOARD_LIST_URL);
    }

    void goEditFromDetail() {
        page.locator("a[href^='/board/edit/']").first().click();

        Locator editForm = page.locator("form[action='/board/update']");
        assertThat(editForm).isVisible();
        assertThat(editForm.locator("input[name='title']")).isVisible();
        assertThat(editForm.locator("textarea[name='content']")).isVisible();
    }



    void openDetailByTitle(String title) {
        page.getByText(title).click();
        assertThat(page).hasURL(BOARD_DETAIL_URL);
    }

    void switchToGuestContext() {
        context.close();
        context = browser.newContext();
        page = context.newPage();

        page.setDefaultTimeout(10_000);
        page.setDefaultNavigationTimeout(30_000);
    }

    void deleteFromDetailAcceptConfirmIfAny() {
        // detail.html에 confirm('삭제할까요?')가 있으니 dialog 대응
        // confirm이 없는 환경이어도 onceDialog는 뜰 때만 동작하므로 안전
        page.onceDialog(Dialog::accept);

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("삭제")).click();

        // 삭제 후 목록으로 리다이렉트
        assertThat(page).hasURL(BOARD_LIST_URL);
    }

    // ---------- tests (TC 6 ~ 18) ----------

    // TC 6. (로그인) 게시글 없는 상태의 게시판 목록 화면
    @Test
    void tc06_board_list_shows_empty_when_no_posts_logged_in() {
        joinAndLogin();
        assertThat(page).hasURL(BOARD_LIST_URL);
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("게시판 목록"))).isVisible();
    }

    // TC 7. (로그인) 게시글 작성 화면 이동
    @Test
    void tc07_board_write_page_open_logged_in() {
        joinAndLogin();

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("게시글 작성하기")).click();
        assertThat(page).hasURL(BOARD_WRITE_URL);

        // 화면에 실제로 "게시글 작성" 헤딩이 있으면 유지
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("게시글 작성"))).isVisible();
    }

    // TC 8. (로그인) 게시글 작성 성공
    @Test
    void tc08_board_create_success_logged_in() {
        joinAndLogin();

        String title = "post_" + System.currentTimeMillis();
        String content = "내용_" + System.currentTimeMillis();

        createPost(title, content);

        assertThat(page).hasURL(BOARD_LIST_URL);
        assertThat(page.getByText(title)).isVisible();
    }

    // TC 9. (로그인) 게시글 상세 화면 진입
    @Test
    void tc09_board_detail_open_logged_in() {
        joinAndLogin();

        String title = "post_" + System.currentTimeMillis();
        String content = "내용_" + System.currentTimeMillis();

        createPost(title, content);
        openDetailByTitle(title);

        // detail.html 기준: <h1 th:text="${board.title}">
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(title))).isVisible();
        assertThat(page.getByText(content)).isVisible();
    }

    @Test
    void tc10_board_edit_page_open_logged_in() {
        joinAndLogin();

        String title = "post_" + System.currentTimeMillis();
        String content = "내용_" + System.currentTimeMillis();

        createPost(title, content);
        openDetailByTitle(title);

        goEditFromDetail();
    }

    @Test
    void tc11_board_edit_success_logged_in() {
        joinAndLogin();

        String title = "post_" + System.currentTimeMillis();
        String content = "내용_" + System.currentTimeMillis();
        String editTitle = title + "_edit";
        String editContent = content + "_edit";

        createPost(title, content);
        openDetailByTitle(title);

        goEditFromDetail();

        Locator editForm = page.locator("form[action*='/board/update']");
        editForm.locator("input[name='title']").fill(editTitle);
        editForm.locator("textarea[name='content']").fill(editContent);

        page.waitForNavigation(() -> {
            editForm.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("수정 저장")).click();
        });

        assertThat(page).hasURL(BOARD_LIST_URL);
        assertThat(page.getByText(editTitle)).isVisible();
    }

    // TC 12. (로그인) 게시글 삭제 화면 진입  -> 현재 UI는 “삭제 페이지 이동”이 아니라 “상세에서 바로 삭제(POST)” 구조
    @Test
    void tc12_board_delete_button_visible_on_detail_logged_in() {
        joinAndLogin();

        String title = "post_" + System.currentTimeMillis();
        String content = "내용_" + System.currentTimeMillis();

        createPost(title, content);
        openDetailByTitle(title);

        // detail.html 기준: 삭제는 버튼
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("삭제"))).isVisible();
    }

    // TC 13. (로그인) 게시글 삭제 성공 (detail에서 삭제 버튼 + confirm 처리)
    @Test
    void tc13_board_delete_success_logged_in() {
        joinAndLogin();

        String title = "post_" + System.currentTimeMillis();
        String content = "내용_" + System.currentTimeMillis();

        createPost(title, content);
        openDetailByTitle(title);

        deleteFromDetailAcceptConfirmIfAny();

        // 목록에서 글이 없어야 함
        assertThat(page.getByText(title)).hasCount(0);
    }

    // TC 14. (비로그인) 게시판 목록 접근 가능
    @Test
    void tc14_board_list_accessible_when_not_logged_in() {
        page.navigate(BASE_URL + "/board/list");
        assertThat(page).hasURL(BOARD_LIST_URL);
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("게시판 목록"))).isVisible();
    }

    // TC 15. (비로그인) 작성 화면 접근 시 로그인으로 리다이렉트
    @Test
    void tc15_board_write_redirects_to_login_when_not_logged_in() {
        page.navigate(BASE_URL + "/board/write");
        assertThat(page).hasURL(LOGIN_URL);

        // 실제 헤딩 문구가 다를 수 있어서 URL 위주로 검증
        assertThat(page.locator("#loginId")).isVisible();
        assertThat(page.locator("#password")).isVisible();
    }

    // TC 16. (비로그인) 상세 화면은 접근 가능
    @Test
    void tc16_board_detail_accessible_when_not_logged_in() {
        // 1) 로그인해서 게시글 1개 생성
        joinAndLogin();
        String title = "post_" + System.currentTimeMillis();
        String content = "내용_" + System.currentTimeMillis();
        createPost(title, content);

        // 2) 비로그인(새 컨텍스트)로 전환
        switchToGuestContext();

        // 3) 목록에서 생성한 글로 상세 진입
        page.navigate(BASE_URL + "/board/list");
        openDetailByTitle(title);

        // 4) 상세에서 제목/내용 확인
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(title))).isVisible();
        assertThat(page.getByText(content)).isVisible();
    }

    // TC 17. (비로그인) 수정 시도 시 로그인으로 리다이렉트
    @Test
    void tc17_board_edit_redirects_to_login_when_not_logged_in() {
        joinAndLogin();
        String title = "post_" + System.currentTimeMillis();
        String content = "내용_" + System.currentTimeMillis();
        createPost(title, content);

        switchToGuestContext();

        page.navigate(BASE_URL + "/board/list");
        openDetailByTitle(title);

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("수정")).click();

        assertThat(page).hasURL(LOGIN_URL);
        assertThat(page.locator("#loginId")).isVisible();
        assertThat(page.locator("#password")).isVisible();
    }

    // TC 18. (비로그인) 삭제 시도 시 로그인으로 리다이렉트 + 목록 재조회 시 글 유지
    @Test
    void tc18_board_delete_redirects_to_login_and_post_still_exists_when_not_logged_in() {
        joinAndLogin();
        String title = "post_" + System.currentTimeMillis();
        String content = "내용_" + System.currentTimeMillis();
        createPost(title, content);

        switchToGuestContext();

        page.navigate(BASE_URL + "/board/list");
        openDetailByTitle(title);

        // confirm이 뜨는 경우 대비
        page.onceDialog(Dialog::accept);

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("삭제")).click();

        assertThat(page).hasURL(LOGIN_URL);
        assertThat(page.locator("#loginId")).isVisible();

        // "삭제 안 됐음" 검증: 다시 목록 접근해서 title이 보이는지 확인
        page.navigate(BASE_URL + "/board/list");
        assertThat(page.getByText(title)).isVisible();
    }

    // TC 19. 로그인 후 세션무효화(쿠키 삭제)후 게시글 작성화면 접근 불가
    @Test
    void tc19_board_write_redirects_to_login_when_cookies_cleared() {
        joinAndLogin();

        // 세션 만료를 재현하기 위해 쿠키를 삭제하여 테스트 진행
        context.clearCookies();

        // 새 브라우저로 컨텍스트 전환(쿠키 삭제 후 로그인정보가 남아있는 것을 방지)
        switchToGuestContext();

        page.navigate(BASE_URL + "/board/write");

        // 로그인화면으로 리다이렉트 확인
        assertThat(page).hasURL(LOGIN_URL);
        assertThat(page.locator("#loginId")).isVisible();
        assertThat(page.locator("#password")).isVisible();
    }

    // TC 20. 게시글 등록 버튼을 연속 클릭 테스트
    @Test
    void tc20_post_saved_butten_double_click() {
        joinAndLogin();

        String title = "post_" + System.currentTimeMillis();
        String content = "내용_" + System.currentTimeMillis();

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("게시글 작성하기")).click();
        assertThat(page).hasURL(BOARD_WRITE_URL);

        page.locator("input[name='title']").fill(title);
        page.locator("textarea[name='content']").fill(content);

        // When: 등록 버튼 연속 클릭(중복 요청 유도)
        Locator submit = page.locator("button[type='submit'], input[type='submit']");
        assertThat(submit).isVisible();

        // When: 빠른 연속 클릭(첫 클릭 후 네비게이션 발생 가능)
        submit.click();
        try {
            submit.click();
        } catch (Exception ignored) {
            // 이미 페이지가 이동되었을 수 있으므로 무시
        }

        // Then: 목록으로 이동 + 글은 1개만 존재
        assertThat(page).hasURL(BOARD_LIST_URL);
        assertThat(page.getByText(title)).hasCount(1);
    }

    // TC 21. 게시글 등록 입력값 검증
    @Test
    void tc21_post_not_submitted_when_title_is_empty() {
        joinAndLogin();

        String content = "내용_" + System.currentTimeMillis();

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("게시글 작성하기")).click();
        assertThat(page).hasURL(BOARD_WRITE_URL);

        page.locator("input[name='title']").fill("");
        page.locator("textarea[name='content']").fill(content);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("등록")).click();

        // Then: 제목 input이 invalid 상태인지 확인
        Locator invalidTitle = page.locator("input[name='title']:invalid");
        assertThat(invalidTitle).isVisible();

        //페이지 이동이 안 됐는지도 같이 확인
        assertThat(page).hasURL(BOARD_WRITE_URL);;
    }
}
