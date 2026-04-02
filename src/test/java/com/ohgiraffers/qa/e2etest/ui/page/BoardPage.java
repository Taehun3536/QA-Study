package com.ohgiraffers.qa.e2etest.ui.page;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class BoardPage {
    private final Page page;
    private final Locator writeNavLink;
    private final Locator titleInput;
    private final Locator contentInput;
    private final Locator submitButton;
    private final Locator editButton;
    private final Locator deleteButton;

    public BoardPage(Page page) {
        this.page = page;
        this.writeNavLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("게시글 작성하기"));
        this.titleInput = page.locator("input[name='title']");
        this.contentInput = page.locator("textarea[name='content']");
        this.submitButton = page.locator("button[type='submit'], input[type='submit']");
        this.editButton = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("수정"));
        this.deleteButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("삭제"));
    }

    public void navigateToList() { page.navigate("http://localhost:8080/board/list"); }

    public void createPost(String title, String content) {
        writeNavLink.click();
        titleInput.fill(title);
        contentInput.fill(content);
        submitButton.first().click();
    }

    public void openDetail(String title) {
        page.getByText(title).click();
    }

    public void updatePost(String newTitle, String newContent) {
        editButton.click();
        titleInput.fill(newTitle);
        contentInput.fill(newContent);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("수정 저장")).click();
    }

    public void deletePostWithConfirm() {
        page.onceDialog(Dialog::accept);
        deleteButton.click();
    }
}