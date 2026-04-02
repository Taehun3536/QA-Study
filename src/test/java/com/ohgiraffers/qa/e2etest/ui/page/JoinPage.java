package com.ohgiraffers.qa.e2etest.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class JoinPage {
    private final Page page;
    private final String baseUrl;
    private final Locator loginIdInput;
    private final Locator passwordInput;
    private final Locator nicknameInput;
    private final Locator submitButton;

    public JoinPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
        this.loginIdInput = page.locator("input[name='loginId']");
        this.passwordInput = page.locator("input[name='password']");
        this.nicknameInput = page.locator("input[name='nickname']");
        this.submitButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("가입하기"));
    }

    public void navigate() {
        page.navigate(baseUrl + "/user/join");
    }

    public void join(String id, String pw, String nickname) {
        loginIdInput.fill(id);
        passwordInput.fill(pw);
        nicknameInput.fill(nickname);
        submitButton.click();
    }
}