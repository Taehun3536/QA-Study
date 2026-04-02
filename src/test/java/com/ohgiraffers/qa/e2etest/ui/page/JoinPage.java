package com.ohgiraffers.qa.e2etest.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class JoinPage {
    private final Page page;
    private final Locator loginIdInput;
    private final Locator passwordInput;
    private final Locator nicknameInput;
    private final Locator submitButton;

    public JoinPage(Page page) {
        this.page = page;
        this.loginIdInput = page.locator("input[name='loginId']");
        this.passwordInput = page.locator("input[name='password']");
        this.nicknameInput = page.locator("input[name='nickname']");
        this.submitButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("가입하기"));
    }

    public void navigate() {
        page.navigate("http://localhost:8080/user/join");
    }

    public void join(String id, String pw, String nickname) {
        loginIdInput.fill(id);
        passwordInput.fill(pw);
        nicknameInput.fill(nickname);
        submitButton.click();
    }
}