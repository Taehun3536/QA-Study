package com.ohgiraffers.qa.e2etest.ui.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LoginPage {
    private final Page page;
    private final Locator loginIdInput;
    private final Locator passwordInput;
    private final Locator loginButton;

    public LoginPage(Page page) {
        this.page = page;
        this.loginIdInput = page.locator("input[name='loginId']");
        this.passwordInput = page.locator("input[name='password']");
        this.loginButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("로그인"));
    }

    public void navigate() {
        page.navigate("http://localhost:8080/user/login");
    }

    public void login(String id, String pw) {
        loginIdInput.fill(id);
        passwordInput.fill(pw);
        loginButton.click();
    }
}