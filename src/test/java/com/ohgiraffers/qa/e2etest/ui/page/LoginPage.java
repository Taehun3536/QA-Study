package com.ohgiraffers.qa.e2etest.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LoginPage {
    private final Page page;
    private final String baseUrl;
    private final Locator loginIdInput;
    private final Locator passwordInput;
    private final Locator loginButton;

    public LoginPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
        this.loginIdInput = page.locator("input[name='loginId']");
        this.passwordInput = page.locator("input[name='password']");
        this.loginButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("로그인"));
    }

    public void navigate() {
        page.navigate(baseUrl + "/user/login");
    }

    public void login(String id, String pw) {
        loginIdInput.fill(id);
        passwordInput.fill(pw);
        loginButton.click();
    }
}