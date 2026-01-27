package com.ohgiraffers.qa.e2etest.ui.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public final class DbCleaner {

    // application.yml 기준
    private static final String DB_URL = "jdbc:mysql://localhost:3306/jpa_lecture";
    private static final String DB_USER = "iroha";
    private static final String DB_PW = "iroha";

    private DbCleaner() {}

    /** 게시글 전체 삭제 */
    public static void deleteAllBoards() {
        exec("DELETE FROM board");
    }

    /** 테스트에서 만든 계정만 삭제 (prefix 기준) */
    public static void deleteTestUsers() {
        exec("DELETE FROM user WHERE login_id LIKE 'test\\_%' OR login_id LIKE 'dup\\_%'");
    }

    /** 테스트에서 만든 게시글만 삭제 (prefix 기준) */
    public static void deleteTestBoardsByPrefix() {
        exec("DELETE FROM board WHERE title LIKE 'post\\_%'");
    }

    /** 테스트 종료 후: 게시글 + 테스트유저 정리 */
    public static void cleanupAfterE2E() {
        // FK가 있으면 순서 중요할 수 있어서 board 먼저 지움
        deleteTestBoardsByPrefix();
        deleteTestUsers();
    }

    private static void exec(String sql) {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PW);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("[DbCleaner] failed: " + e.getMessage());
        }
    }
}
